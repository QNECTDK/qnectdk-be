package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 캐릭터 카탈로그 항목(GET /api/profiles/characters) 응답.
 * 이미지 에셋은 프론트가 characterId로 매핑하므로 식별자만 내려준다.
 */
public record CharacterResponse(

        @Schema(description = "캐릭터 식별자", example = "character07")
    String characterId
) {
}
