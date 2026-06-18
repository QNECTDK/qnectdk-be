package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShareResponse(
        @Schema(description = "QR/공유용 고유 코드")
        String publicCode,
        @Schema(description = "공유용 전체 URL")
        String shareUrl
) {
}
