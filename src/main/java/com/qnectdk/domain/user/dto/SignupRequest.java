package com.qnectdk.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignupRequest(

        @NotBlank
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phone,

        @NotBlank
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "비밀번호는 영문과 숫자를 포함해야 합니다.")
        String password,

        @NotBlank
        @Size(max = 20)
        String name,

        @NotNull
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate
) {
}
