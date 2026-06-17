package com.qnectdk.domain.friend.repository;

import com.qnectdk.domain.friend.entity.FriendMemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendMemoRepository extends JpaRepository<FriendMemo, Long> {
}