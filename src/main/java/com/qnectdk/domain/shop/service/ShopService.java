package com.qnectdk.domain.shop.service;

import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.shop.dto.ShopItemResponse;
import com.qnectdk.domain.shop.dto.UserItemResponse;
import com.qnectdk.domain.shop.entity.ShopItem;
import com.qnectdk.domain.shop.entity.UserItem;
import com.qnectdk.domain.shop.repository.ShopItemRepository;
import com.qnectdk.domain.shop.repository.UserItemRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserItemRepository userItemRepository;
    private final PointService pointService;

    public List<ShopItemResponse> getItems() {
        return shopItemRepository.findByIsActiveTrue().stream()
                .map(ShopItemResponse::from).toList();
    }

    public List<UserItemResponse> getMyItems(Long userId) {
        return userItemRepository.findByUserId(userId).stream()
                .map(UserItemResponse::from).toList();
    }

    @Transactional
    public UserItemResponse purchase(Long userId, Long itemId) {
        ShopItem item = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        if (!item.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (userItemRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT);
        }

        pointService.spend(userId, item.getPrice(), PointReason.SHOP_PURCHASE, itemId);

        // 구매 시 ShopItem의 type을 복사해서 저장
        UserItem saved = userItemRepository.save(UserItem.of(userId, itemId, item.getType()));
        return UserItemResponse.from(saved);
    }

    // 장착 — 같은 type의 기존 장착을 모두 해제하고 이것만 장착 (한 개만 true 보장)
    @Transactional
    public void equip(Long userId, Long userItemId) {
        UserItem target = userItemRepository.findById(userItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        if (!target.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 같은 type에서 현재 장착된 것들 모두 해제
        List<UserItem> currentlyEquipped =
                userItemRepository.findByUserIdAndTypeAndIsEquippedTrue(userId, target.getType());
        currentlyEquipped.forEach(UserItem::unequip);

        // 이것만 장착
        target.equip();
    }

    // 해제 — 기본(띠 캐릭터)으로 되돌리기
    @Transactional
    public void unequip(Long userId, Long userItemId) {
        UserItem target = userItemRepository.findById(userItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        if (!target.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        target.unequip();
    }
}