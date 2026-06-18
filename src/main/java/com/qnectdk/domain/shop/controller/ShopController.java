package com.qnectdk.domain.shop.controller;

import com.qnectdk.domain.shop.dto.ShopItemResponse;
import com.qnectdk.domain.shop.dto.UserItemResponse;
import com.qnectdk.domain.shop.service.ShopService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // 상점 아이템 목록 (판매중)
    @GetMapping("/items")
    public ApiResponse<List<ShopItemResponse>> getItems() {
        return ApiResponse.ok(shopService.getItems());
    }

    // 내 보유 아이템
    @GetMapping("/my-items")
    public ApiResponse<List<UserItemResponse>> getMyItems(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(shopService.getMyItems(user.getUserId()));
    }

    // 아이템 구매 (포인트 차감 + 보유 추가)
    @PostMapping("/items/{itemId}/purchase")
    public ApiResponse<UserItemResponse> purchase(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long itemId
    ) {
        return ApiResponse.ok(shopService.purchase(user.getUserId(), itemId));
    }

    // 아이템 적용 (같은 type 중 이것만 장착됨)
    @PatchMapping("/my-items/{userItemId}/equip")
    public ApiResponse<Void> equip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userItemId
    ) {
        shopService.equip(user.getUserId(), userItemId);
        return ApiResponse.ok();
    }

    // 아이템 해제 (기본 띠 캐릭터로 되돌림)
    @PatchMapping("/my-items/{userItemId}/unequip")
    public ApiResponse<Void> unequip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userItemId
    ) {
        shopService.unequip(user.getUserId(), userItemId);
        return ApiResponse.ok();
    }
}