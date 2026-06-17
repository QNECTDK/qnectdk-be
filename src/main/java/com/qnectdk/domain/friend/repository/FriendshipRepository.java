package com.qnectdk.domain.friend.repository;

import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

    List<Friendship> findByRequesterIdAndStatus(Long requesterId, FriendshipStatus status);

    // 내 친구 목록: 내가 보냈든 받았든 ACCEPTED인 관계 전부
    @Query("""
            select f from Friendship f
            where f.status = :status
              and (f.requesterId = :userId or f.addresseeId = :userId)
            """)
    List<Friendship> findAcceptedFriendsOf(
            @Param("userId") Long userId,
            @Param("status") FriendshipStatus status
    );
}