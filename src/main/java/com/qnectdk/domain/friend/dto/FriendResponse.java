package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.friend.entity.Friendship;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 친구 관계(방향성) 응답. 한 행 = "내가 friendId 를 친구로 저장".
 */
public record FriendResponse(
        @Schema(description = "친구 관계(내 방향) id", example = "1")
        Long friendshipId,

        @Schema(description = "친구의 userId", example = "2")
        Long friendId,

        @Schema(description = "친구로 저장된 시각", example = "2026-06-19T14:28:36")
        LocalDateTime createdAt
) {
    public static FriendResponse from(Friendship f) {
        return new FriendResponse(f.getId(), f.getFriendId(), f.getCreatedAt());
    }
}
