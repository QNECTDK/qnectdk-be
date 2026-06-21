package com.qnectdk.domain.shop.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shop_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type;

    @Column(name = "character_id", length = 20)
    private String characterId;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    private ShopItem(String name, ItemType type, String characterId, int price, boolean isActive) {
        this.name = name;
        this.type = type;
        this.characterId = characterId;
        this.price = price;
        this.isActive = isActive;
    }

    public static ShopItem create(String name, ItemType type, String characterId, int price) {
        return ShopItem.builder()
                .name(name)
                .type(type)
                .characterId(characterId)
                .price(price)
                .isActive(true)
                .build();
    }
}