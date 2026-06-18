package com.qnectdk.domain.shop.dto;

import com.qnectdk.domain.shop.entity.ItemType;
import com.qnectdk.domain.shop.entity.UserItem;

public record UserItemResponse(
        Long userItemId,
        Long itemId,
        ItemType type,
        boolean isEquipped
) {
    public static UserItemResponse from(UserItem ui) {
        return new UserItemResponse(
                ui.getId(),
                ui.getItemId(),
                ui.getType(),
                ui.isEquipped()
        );
    }
}