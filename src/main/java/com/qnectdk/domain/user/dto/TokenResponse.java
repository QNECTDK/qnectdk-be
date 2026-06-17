package com.qnectdk.domain.user.dto;

public record TokenResponse(String tokenType, String accessToken, String refreshToken) {

    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse("Bearer", accessToken, refreshToken);
    }
}
