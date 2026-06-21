package com.qnectdk.domain.friend.repository;

import com.qnectdk.domain.friend.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

  /** ownerId 가 friendId 를 이미 친구로 저장했는지. */
  boolean existsByOwnerIdAndFriendId(Long ownerId, Long friendId);

    /** ownerId 의 친구 목록(내가 저장한 친구들). */
    List<Friendship> findByOwnerId(Long ownerId);

    /** 특정 방향 한 행 조회(삭제·단건 처리용). */
    Optional<Friendship> findByOwnerIdAndFriendId(Long ownerId, Long friendId);
  }
