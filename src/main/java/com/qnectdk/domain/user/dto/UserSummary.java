package com.qnectdk.domain.user.dto;

import com.qnectdk.domain.user.entity.User;

import java.time.LocalDate;

/**
 * 다른 도메인(profile 등)에 노출하는 사용자 읽기 모델.
 * 타 도메인은 User 엔티티 대신 이 DTO와 UserQueryService를 통해 사용자 정보를 얻는다.
 */
public record UserSummary(Long userId, String name, LocalDate birthDate, String publicCode) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getName(), user.getBirthDate(), user.getPublicCode());
    }
}
