package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 캐릭터 선택으로 프로필 이미지 설정(PUT /api/profiles/me/image) 요청.
 */
public record ProfileImageRequest(

        @Schema(description = "선택한 캐릭터 식별자", example = "character07")
        @NotBlank
        String characterId
) {
}
