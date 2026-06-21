package com.qnectdk.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FriendSummary(
        @Schema(description = "상대방 userId", example = "2")
        Long friendId,   // 상대방 userId

        @Schema(description = "상대방 이름 (자동완성 표시용)", example = "이영희")
        String name,     // 상대방 이름 (자동완성 표시용)

        @Schema(description = "상대방 캐릭터 식별자 (미설정 시 띠 기본값)", example = "character07")
        String characterId
) {
}