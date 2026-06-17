package com.qnectdk.domain.profile.dto;

import com.qnectdk.domain.profile.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 프로필 생성(POST)/수정(PUT) 공용 요청. 두 작업의 입력 형태가 동일하다.
 */
public record ProfileRequest(

        @NotBlank
        @Size(max = 30)
        String school,

        @NotNull
        Gender gender,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{4}$", message = "MBTI는 영문 4자여야 합니다.")
        String mbti,

        @NotBlank
        @Size(max = 20)
        String drinkLevel,

        @Size(max = 30)
        String favoriteFood
) {
}
