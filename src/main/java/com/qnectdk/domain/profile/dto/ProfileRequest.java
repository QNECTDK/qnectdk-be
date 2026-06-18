package com.qnectdk.domain.profile.dto;

import com.qnectdk.domain.profile.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 프로필 업서트(PUT /api/profiles/me) 요청. 온보딩·수정 공용.
 */
public record ProfileRequest(

        @Schema(description = "학교명", example = "국민대학교")
        @NotBlank
        @Size(max = 30)
        String school,

        @Schema(description = "성별", example = "MALE")
        @NotNull
        Gender gender,

        @Schema(description = "MBTI(영문 4자)", example = "ENFP")
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{4}$", message = "MBTI는 영문 4자여야 합니다.")
        String mbti,

        @Schema(description = "주량", example = "소주 3잔")
        @NotBlank
        @Size(max = 20)
        String drinkLevel,

        @Schema(description = "좋아하는 음식", example = "치킨")
        @Size(max = 30)
        String favoriteFood
) {
}
