package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.profile.dto.PersonCard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FriendCardResponse(
        @Schema(description = "친구 관계 id", example = "1")
        Long friendshipId,

        @Schema(description = "친구가 된 시각(수락 시각, 없으면 요청 생성 시각)", example = "2026-06-19T14:28:36")
        LocalDateTime savedAt,

        @Schema(description = "친구의 person 카드")
        PersonCard person
) {
}
