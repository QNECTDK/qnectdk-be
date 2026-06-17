package com.qnectdk.domain.group.dto;

import com.qnectdk.domain.group.entity.FriendGroup;

import java.time.LocalDateTime;

public record GroupResponse(
        Long groupId,
        Long userId,
        String name,
        String hashtags,
        LocalDateTime createdAt
) {
    public static GroupResponse from(FriendGroup g) {
        return new GroupResponse(
                g.getId(),
                g.getUserId(),
                g.getName(),
                g.getHashtags(),
                g.getCreatedAt()
        );
    }
}