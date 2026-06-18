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

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    private ShopItem(String name, ItemType type, String imageUrl, int price, boolean isActive) {
        this.name = name;
        this.type = type;
        this.imageUrl = imageUrl;
        this.price = price;
        this.isActive = isActive;
    }

    public static ShopItem create(String name, ItemType type, String imageUrl, int price) {
        return ShopItem.builder()
                .name(name)
                .type(type)
                .imageUrl(imageUrl)
                .price(price)
                .isActive(true)
                .build();
    }
}