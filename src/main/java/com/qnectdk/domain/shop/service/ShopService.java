package com.qnectdk.domain.shop.service;

import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.domain.shop.dto.ShopItemResponse;
import com.qnectdk.domain.shop.dto.UserItemResponse;
import com.qnectdk.domain.shop.entity.ItemType;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserItemRepository userItemRepository;
    private final PointService pointService;
    private final ProfileService profileService;

    public List<ShopItemResponse> getItems() {
        return shopItemRepository.findByIsActiveTrue().stream()
                .map(ShopItemResponse::from).toList();
    }

    public List<UserItemResponse> getMyItems(Long userId) {
      List<UserItem> items = userItemRepository.findByUserId(userId);
      if (items.isEmpty()) {
        return List.of();
      }
      // 보유 아이템들의 characterId를 한 번의 IN 조회로 매핑 (N+1 방지)
      Set<Long> itemIds = items.stream().map(UserItem::getItemId).collect(Collectors.toSet());
      Map<Long, String> characterIdByItemId = shopItemRepository.findAllById(itemIds).stream()
          .collect(Collectors.toMap(ShopItem::getId, ShopItem::getCharacterId));
      return items.stream()
          .map(ui -> UserItemResponse.of(ui, characterIdByItemId.get(ui.getItemId())))
          .toList();
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
        return UserItemResponse.of(saved, item.getCharacterId());
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

        // 캐릭터 아이템이면 프로필에 반영 (GET /profiles/me 의 characterId가 장착 캐릭터로 바뀜)
        if (target.getType() == ItemType.CHARACTER) {
          String characterId = shopItemRepository.findById(target.getItemId())
              .map(ShopItem::getCharacterId)
              .orElse(null);
          profileService.applyCharacter(userId, characterId);
        }
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

        // 캐릭터 해제 시 프로필을 띠 기본 캐릭터로 되돌린다(characterId=null → 띠 기본 폴백)
        if (target.getType() == ItemType.CHARACTER) {
          profileService.applyCharacter(userId, null);
          }
        }
  }
