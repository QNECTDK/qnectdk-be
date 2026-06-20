package com.qnectdk.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FriendRequestDto(
        @Schema(description = "친구 요청을 받을 상대방 userId", example = "2")
        @NotNull(message = "addresseeId는 필수입니다.")
        Long addresseeId
) {
}