package com.qnectdk.domain.friend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FriendMemoRequest(
        @NotNull(message = "friendId는 필수입니다.")
        Long friendId,

        @Size(max = 200, message = "메모는 200자 이내여야 합니다.")
        String content
) {
}