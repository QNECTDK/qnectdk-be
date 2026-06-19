package com.qnectdk.domain.user.service;

import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.domain.user.repository.UserRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

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

    /**
     * 여러 사용자를 한 번의 IN 쿼리로 조회한다(타 도메인의 N+1 방지용 일괄 조회 통로).
     * 존재하지 않는 id 는 결과에서 생략된다.
     */
    public List<UserSummary> getByIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(UserSummary::from)
                .toList();
    }

    /**
     * 전체 사용자 ID 목록을 조회한다(데일리 알림 팬아웃 등 전체 사용자 순회용).
     * 엔티티 전체 로딩을 피하기 위해 ID만 select 한다.
     */
    public List<Long> getAllUserIds() {
        return userRepository.findAllUserIds();
    }
}
