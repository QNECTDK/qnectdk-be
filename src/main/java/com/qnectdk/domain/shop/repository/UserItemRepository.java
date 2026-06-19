package com.qnectdk.domain.shop.repository;

import com.qnectdk.domain.shop.entity.ItemType;
import com.qnectdk.domain.shop.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUserId(Long userId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    List<UserItem> findByUserIdAndTypeAndIsEquippedTrue(Long userId, ItemType type);
}
