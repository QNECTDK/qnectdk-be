package com.qnectdk.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "토큰 타입 (항상 Bearer)") String tokenType,
        @Schema(description = "API 인증에 사용하는 액세스 토큰") String accessToken,
        @Schema(description = "토큰 갱신에 사용하는 리프레시 토큰") String refreshToken) {

    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse("Bearer", accessToken, refreshToken);
    }
}
