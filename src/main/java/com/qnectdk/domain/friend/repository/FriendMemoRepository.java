package com.qnectdk.domain.friend.repository;

import com.qnectdk.domain.friend.entity.FriendMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendMemoRepository extends JpaRepository<FriendMemo, Long> {

    // 내가(owner) 특정 친구에게 단 메모 조회 (메모는 owner+friend 당 1개)
    Optional<FriendMemo> findByOwnerIdAndFriendId(Long ownerId, Long friendId);
}