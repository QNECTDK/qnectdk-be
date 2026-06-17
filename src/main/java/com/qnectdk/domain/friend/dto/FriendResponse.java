package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.entity.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendResponse(
        Long friendshipId,
        Long requesterId,
        Long addresseeId,
        FriendshipStatus status,
        LocalDateTime acceptedAt,
        LocalDateTime createdAt
) {
    public static FriendResponse from(Friendship f) {
        return new FriendResponse(
                f.getId(),
                f.getRequesterId(),
                f.getAddresseeId(),
                f.getStatus(),
                f.getAcceptedAt(),
                f.getCreatedAt()
        );
    }
}