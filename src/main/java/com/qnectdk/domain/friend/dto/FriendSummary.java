package com.qnectdk.domain.friend.dto;

public record FriendSummary(
        Long friendId,   // 상대방 userId
        String name      // 상대방 이름 (자동완성 표시용)
) {
}