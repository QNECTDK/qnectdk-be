package com.qnectdk.domain.friend.dto;

import jakarta.validation.constraints.NotNull;

public record FriendRequestDto(
        @NotNull(message = "addresseeId는 필수입니다.")
        Long addresseeId
) {
}