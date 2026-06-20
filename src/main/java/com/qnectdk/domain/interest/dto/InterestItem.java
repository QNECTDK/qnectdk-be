package com.qnectdk.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record InterestItem(
        @Schema(description = "관심사 ID", example = "1") Long id,
        @Schema(description = "관심사 이름", example = "헬스") String name
) {
}
