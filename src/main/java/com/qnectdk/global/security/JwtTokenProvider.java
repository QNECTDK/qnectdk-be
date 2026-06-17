package com.qnectdk.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity-ms}") long accessTokenValidityMs,
            @Value("${app.jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenValidityMs, TYPE_ACCESS);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityMs, TYPE_REFRESH);
    }

    public long getRefreshTokenValidityMs() {
        return refreshTokenValidityMs;
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    /** 서명·만료가 유효하고 type=access 인 경우에만 true. */
    public boolean validateAccessToken(String token) {
        return validateWithType(token, TYPE_ACCESS);
    }

    /** 서명·만료가 유효하고 type=refresh 인 경우에만 true. (refresh 토큰을 access로 쓰는 것을 차단) */
    public boolean validateRefreshToken(String token) {
        return validateWithType(token, TYPE_REFRESH);
    }

    private boolean validateWithType(String token, String expectedType) {
        try {
            return expectedType.equals(parse(token).get(TYPE_CLAIM, String.class));
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private String createToken(Long userId, long validityMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti: 같은 초에 발급돼도 토큰이 유일하도록 (refresh_tokens.token unique)
                .subject(String.valueOf(userId))
                .claim(TYPE_CLAIM, type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
