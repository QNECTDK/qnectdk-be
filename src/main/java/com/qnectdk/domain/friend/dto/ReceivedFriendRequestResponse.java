package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.friend.entity.FriendshipStatus;
import com.qnectdk.domain.profile.dto.PersonCard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReceivedFriendRequestResponse(
        @Schema(description = "친구 관계 id", example = "1")
        Long friendshipId,

        @Schema(description = "친구 관계 상태")
        FriendshipStatus status,

        @Schema(description = "요청을 보낸 시각", example = "2026-06-18T09:10:00")
        LocalDateTime requestedAt,

        @Schema(description = "요청을 보낸 사람(requester)의 person 카드")
        PersonCard person
) {
}
