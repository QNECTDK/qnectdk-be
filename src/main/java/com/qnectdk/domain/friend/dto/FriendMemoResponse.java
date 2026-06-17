package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.friend.entity.FriendMemo;

import java.time.LocalDateTime;

public record FriendMemoResponse(
        Long memoId,
        Long ownerId,
        Long friendId,
        String content,
        LocalDateTime updatedAt
) {
    public static FriendMemoResponse from(FriendMemo m) {
        return new FriendMemoResponse(
                m.getId(),
                m.getOwnerId(),
                m.getFriendId(),
                m.getContent(),
                m.getUpdatedAt()
        );
    }
}