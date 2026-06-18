package com.qnectdk.domain.shop.dto;

import com.qnectdk.domain.shop.entity.ItemType;
import com.qnectdk.domain.shop.entity.ShopItem;

public record ShopItemResponse(
        Long itemId,
        String name,
        ItemType type,
        String imageUrl,
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