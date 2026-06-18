package com.qnectdk.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignupRequest(

        @Schema(description = "로그인 아이디 (영문 소문자로 시작하는 4~20자)", example = "tester01")
        @NotBlank
        @Pattern(regexp = "^[a-z][a-z0-9_]{3,19}$",
                message = "아이디는 영문 소문자로 시작하는 4~20자(영문 소문자, 숫자, _)여야 합니다.")
        String loginId,

        @Schema(description = "전화번호 (010으로 시작하는 11자리 숫자)", example = "01012345678")
        @NotBlank
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phone,

        @Schema(description = "비밀번호 (영문과 숫자를 포함한 8자 이상)", example = "test1234")
        @NotBlank
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "비밀번호는 영문과 숫자를 포함해야 합니다.")
        String password,

        @Schema(description = "이름 (최대 20자)", example = "홍길동")
        @NotBlank
        @Size(max = 20)
        String name,

        @Schema(description = "생년월일 (과거 날짜)", example = "2003-05-01")
        @NotNull
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate
) {
}
