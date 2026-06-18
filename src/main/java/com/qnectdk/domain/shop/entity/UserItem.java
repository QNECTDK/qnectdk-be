package com.qnectdk.domain.shop.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_item", columnNames = {"user_id", "item_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type; // 장착 그룹 구분용 (구매 시 ShopItem에서 복사)

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped;

    @Builder
    private UserItem(Long userId, Long itemId, ItemType type) {
        this.userId = userId;
        this.itemId = itemId;
        this.type = type;
        this.isEquipped = false;
    }

    public static UserItem of(Long userId, Long itemId, ItemType type) {
        return UserItem.builder()
                .userId(userId)
                .itemId(itemId)
                .type(type)
                .build();
    }

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }
}