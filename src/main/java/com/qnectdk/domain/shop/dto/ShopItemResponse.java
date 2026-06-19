package com.qnectdk.domain.shop.dto;

import com.qnectdk.domain.shop.entity.ItemType;
import com.qnectdk.domain.shop.entity.ShopItem;
import io.swagger.v3.oas.annotations.media.Schema;

public record ShopItemResponse(
        @Schema(description = "상점 아이템 id", example = "1")
        Long itemId,

        @Schema(description = "아이템 이름", example = "호랑이")
        String name,

        @Schema(description = "아이템 종류")
        ItemType type,

        @Schema(description = "아이템 이미지 경로", example = "/characters/tiger.png")
        String imageUrl,

        @Schema(description = "구매 가격(포인트)", example = "200")
        int price
) {
    public static ShopItemResponse from(ShopItem item) {
        return new ShopItemResponse(
                item.getId(),
                item.getName(),
                item.getType(),
                item.getImageUrl(),
                item.getPrice()
        );
    }
}