package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageResponse(
    @Schema(description = "설정된 캐릭터 식별자(프론트가 이미지 매핑)", example = "character07") String characterId
) {
}
