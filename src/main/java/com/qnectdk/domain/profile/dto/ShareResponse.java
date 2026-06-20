package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShareResponse(
        @Schema(description = "QR/공유용 고유 코드", example = "Ab3xYz9Q")
        String publicCode,
        @Schema(description = "공유용 전체 URL", example = "https://qnect.app/Ab3xYz9Q")
        String shareUrl
) {
}
