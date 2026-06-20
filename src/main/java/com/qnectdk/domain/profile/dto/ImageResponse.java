package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageResponse(
        @Schema(description = "업로드된 프로필 이미지 URL", example = "http://localhost:8080/files/3f2a1b9c4e.jpg")
        String imageUrl
) {
}
