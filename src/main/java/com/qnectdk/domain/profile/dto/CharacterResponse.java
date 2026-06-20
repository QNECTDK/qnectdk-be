package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 캐릭터 카탈로그 항목(GET /api/profiles/characters) 응답.
 */
public record CharacterResponse(

        @Schema(description = "캐릭터 식별자", example = "character07")
        String characterId,

        @Schema(description = "캐릭터 이미지 URL", example = "https://cdn.qnect.example.com/characters/07.png")
        String imageUrl
) {
}
