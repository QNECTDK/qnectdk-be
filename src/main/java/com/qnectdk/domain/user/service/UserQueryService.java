package com.qnectdk.domain.user.service;

import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.domain.user.repository.UserRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 읽기 전용 조회 API. 타 도메인(profile/interest)이 Long ID로 사용자 정보를 얻는 통로.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    public UserSummary getById(Long userId) {
        return UserSummary.from(userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)));
    }

    public UserSummary getByPublicCode(String publicCode) {
        return UserSummary.from(userRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)));
    }
}
