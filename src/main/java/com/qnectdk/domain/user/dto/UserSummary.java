package com.qnectdk.domain.user.dto;

import com.qnectdk.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 다른 도메인(profile 등)에 노출하는 사용자 읽기 모델.
 * 타 도메인은 User 엔티티 대신 이 DTO와 UserQueryService를 통해 사용자 정보를 얻는다.
 */
public record UserSummary(
        @Schema(description = "사용자 id", example = "1")
        Long userId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "생년월일", example = "2003-05-01")
        LocalDate birthDate,

        @Schema(description = "QR/공유용 고유 코드", example = "AB12CD34")
        String publicCode) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getName(), user.getBirthDate(), user.getPublicCode());
    }
}
