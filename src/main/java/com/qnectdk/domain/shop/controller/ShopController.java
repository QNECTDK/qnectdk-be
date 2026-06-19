package com.qnectdk.domain.shop.controller;

import com.qnectdk.domain.shop.dto.ShopItemResponse;
import com.qnectdk.domain.shop.dto.UserItemResponse;
import com.qnectdk.domain.shop.service.ShopService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상점", description = "캐릭터/QR 꾸미기 아이템 구매·적용 API")
@RestController // 캐릭터를 아이템으로 할께용..
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "상점 아이템 목록", description = "판매중인 캐릭터(200P)·QR 꾸미기(150P)을 반환한다.")
    @GetMapping("/items")
    public ApiResponse<List<ShopItemResponse>> getItems() {
        return ApiResponse.ok(shopService.getItems());
    }

    @Operation(summary = "내 보유 아이템", description = "내가 구매한 아이템과 적용 상태를 반환한다.")
    @GetMapping("/my-items")
    public ApiResponse<List<UserItemResponse>> getMyItems(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(shopService.getMyItems(user.getUserId()));
    }

    @Operation(summary = "아이템 구매", description = "포인트로 아이템을 구매한다. 잔액 부족 시 INSUFFICIENT_POINT, 이미 보유 시 거부.")
    @PostMapping("/items/{itemId}/purchase")
    public ApiResponse<UserItemResponse> purchase(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long itemId
    ) {
        return ApiResponse.ok(shopService.purchase(user.getUserId(), itemId));
    }

    @Operation(summary = "아이템 적용", description = "프로필에서 하나만 적용할 수 있으며, 새로운 캐릭터를 프로필에 적용하면 기존 프로필에 적용된 캐릭터는 자동으로 해제됩니다.")
    @PatchMapping("/my-items/{userItemId}/equip")
    public ApiResponse<Void> equip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userItemId
    ) {
        shopService.equip(user.getUserId(), userItemId);
        return ApiResponse.ok();
    }

    @Operation(summary = "아이템 해제", description = "아이템을 해제한다. 캐릭터 해제 시 기본 띠 캐릭터로 복귀.")
    @PatchMapping("/my-items/{userItemId}/unequip")
    public ApiResponse<Void> unequip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userItemId
    ) {
        shopService.unequip(user.getUserId(), userItemId);
        return ApiResponse.ok();
    }
}