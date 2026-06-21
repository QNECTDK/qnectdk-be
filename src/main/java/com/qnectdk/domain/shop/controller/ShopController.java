package com.qnectdk.domain.shop.controller;

import com.qnectdk.domain.shop.dto.ShopItemResponse;
import com.qnectdk.domain.shop.dto.UserItemResponse;
import com.qnectdk.domain.shop.service.ShopService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상점", description = """
        캐릭터 아이템을 포인트로 구매·장착. 장착 = 그 캐릭터를 내 프로필 사진으로 등록.
        [흐름] 목록(GET /items) → 구매(POST /items/{id}/purchase, 포인트 차감) → 보유(GET /my-items) → 장착/해제.
        캐릭터: 생년월일 띠 캐릭터가 기본 프사(무료). 17종 전부 200P 구매 가능.
        장착은 type별 하나만 — 다른 캐릭터를 장착하면 기존 것은 자동 해제(= 캐릭터 교체).
        해제하면 장착이 풀리고 기본 띠 캐릭터로 복귀. characterId는 캐릭터 식별용 코드이며, 실제 이미지는 프론트에서 관리한다.
        """)
@RestController // 캐릭터를 아이템으로 할께용..
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "상점 아이템 목록", description = "판매중인 캐릭터(200P)를 반환한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @GetMapping("/items")
    public ApiResponse<List<ShopItemResponse>> getItems() {
        return ApiResponse.ok(shopService.getItems());
    }

    @Operation(summary = "내 보유 아이템", description = "내가 구매한 아이템과 적용 상태를 반환한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @GetMapping("/my-items")
    public ApiResponse<List<UserItemResponse>> getMyItems(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(shopService.getMyItems(user.getUserId()));
    }

    @Operation(summary = "아이템 구매", description = "포인트로 아이템을 구매한다. 잔액 부족 시 INSUFFICIENT_POINT, 이미 보유 시 거부.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "포인트 잔액 부족 (INSUFFICIENT_POINT)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 보유한 아이템 (RESOURCE_CONFLICT)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않거나 판매 중지된 아이템 (INVALID_INPUT)")
    })
    @PostMapping("/items/{itemId}/purchase")
    public ApiResponse<UserItemResponse> purchase(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "구매할 상점 아이템 id", example = "1") @PathVariable Long itemId
    ) {
        return ApiResponse.ok(shopService.purchase(user.getUserId(), itemId));
    }

    @Operation(summary = "아이템 적용", description = "프로필에서 하나만 적용할 수 있으며, 새로운 캐릭터를 프로필에 적용하면 기존 프로필에 적용된 캐릭터는 자동으로 해제됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 보유한 아이템이 아님 (ACCESS_DENIED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "보유 아이템을 찾을 수 없음 (INVALID_INPUT)")
    })
    @PatchMapping("/my-items/{userItemId}/equip")
    public ApiResponse<Void> equip(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "장착할 보유 아이템 id", example = "1") @PathVariable Long userItemId
    ) {
        shopService.equip(user.getUserId(), userItemId);
        return ApiResponse.ok();
    }

    @Operation(summary = "아이템 해제", description = "아이템을 해제한다. 캐릭터 해제 시 기본 띠 캐릭터로 복귀.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 보유한 아이템이 아님 (ACCESS_DENIED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "보유 아이템을 찾을 수 없음 (INVALID_INPUT)")
    })
    @PatchMapping("/my-items/{userItemId}/unequip")
    public ApiResponse<Void> unequip(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "해제할 보유 아이템 id", example = "1") @PathVariable Long userItemId
    ) {
        shopService.unequip(user.getUserId(), userItemId);
        return ApiResponse.ok();
    }
}