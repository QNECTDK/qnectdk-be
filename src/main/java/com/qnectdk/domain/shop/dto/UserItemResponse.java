package com.qnectdk.domain.shop.dto;

import com.qnectdk.domain.shop.entity.ItemType;
import com.qnectdk.domain.shop.entity.UserItem;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserItemResponse(
        @Schema(description = "보유 아이템 id", example = "1")
        Long userItemId,

        @Schema(description = "원본 상점 아이템 id", example = "1")
        Long itemId,

        @Schema(description = "캐릭터 식별자(프론트가 이미지 매핑). 캐릭터 외 아이템은 null", example = "character03")
        String characterId,

        @Schema(description = "아이템 종류")
        ItemType type,

        @Schema(description = "현재 장착 여부", example = "true")
        boolean isEquipped
) {
    public static UserItemResponse of(UserItem ui, String characterId) {
        return new UserItemResponse(
                ui.getId(),
                ui.getItemId(),
                characterId,
                ui.getType(),
                ui.isEquipped()
        );
    }
}
