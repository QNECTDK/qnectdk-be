package com.qnectdk.domain.friend.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 관계 상태")
public enum FriendshipStatus {
    @Schema(description = "요청 보냄, 상대 수락 대기중") PENDING,
    @Schema(description = "친구 추가됨") ACCEPTED,
    @Schema(description = "거절됨") REJECTED
}