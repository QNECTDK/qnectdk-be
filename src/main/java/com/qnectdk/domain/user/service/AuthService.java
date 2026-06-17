package com.qnectdk.domain.user.service;

import com.qnectdk.domain.user.dto.LoginRequest;
import com.qnectdk.domain.user.dto.SignupRequest;
import com.qnectdk.domain.user.dto.SignupResponse;
import com.qnectdk.domain.user.dto.TokenResponse;
import com.qnectdk.domain.user.entity.RefreshToken;
import com.qnectdk.domain.user.entity.User;
import com.qnectdk.domain.user.repository.RefreshTokenRepository;
import com.qnectdk.domain.user.repository.UserRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import com.qnectdk.global.security.JwtTokenProvider;
import com.qnectdk.global.util.PublicCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String LOGIN_ID_REGEX = "^[a-z][a-z0-9_]{3,19}$";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PublicCodeGenerator publicCodeGenerator;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }
        User user = User.create(
                request.loginId(),
                request.phone(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.birthDate(),
                generateUniquePublicCode());
        return SignupResponse.from(userRepository.save(user));
    }

    public boolean isLoginIdAvailable(String loginId) {
        if (loginId == null || !loginId.matches(LOGIN_ID_REGEX)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return !userRepository.existsByLoginId(loginId);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user.getId());
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        // 회전: 제시된 토큰은 성공/실패와 무관하게 즉시 폐기
        refreshTokenRepository.delete(saved);
        if (saved.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return issueTokens(saved.getUserId());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    private TokenResponse issueTokens(Long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(jwtTokenProvider.getRefreshTokenValidityMs(), ChronoUnit.MILLIS);
        refreshTokenRepository.save(RefreshToken.issue(userId, refreshToken, expiresAt));
        return TokenResponse.of(accessToken, refreshToken);
    }

    private String generateUniquePublicCode() {
        String code;
        do {
            code = publicCodeGenerator.generate();
        } while (userRepository.existsByPublicCode(code));
        return code;
    }
}
