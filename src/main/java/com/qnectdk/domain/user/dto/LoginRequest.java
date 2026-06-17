package com.qnectdk.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "로그인 아이디", example = "tester01")
        @NotBlank
        String loginId,

        @Schema(description = "비밀번호", example = "test1234")
        @NotBlank
        String password
) {
}
