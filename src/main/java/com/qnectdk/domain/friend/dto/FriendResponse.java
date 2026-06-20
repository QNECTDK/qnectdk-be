package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.entity.FriendshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FriendResponse(
        @Schema(description = "친구 관계 id", example = "1")
        Long friendshipId,

        @Schema(description = "친구 요청을 보낸 사람의 userId", example = "1")
        Long requesterId,

        @Schema(description = "친구 요청을 받은 사람의 userId", example = "2")
        Long addresseeId,

        @Schema(description = "친구 관계 상태")
        FriendshipStatus status,

        @Schema(description = "수락된 시각 (수락 전이면 null)", example = "2026-06-19T14:28:36")
        LocalDateTime acceptedAt,

        @Schema(description = "친구 요청을 보낸 시각", example = "2026-06-18T09:10:00")
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