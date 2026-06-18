package com.qnectdk.domain.shop.repository;

import com.qnectdk.domain.shop.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    // 판매중인 아이템만
    List<ShopItem> findByIsActiveTrue();
}
