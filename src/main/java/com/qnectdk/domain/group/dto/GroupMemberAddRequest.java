package com.qnectdk.domain.group.dto;

import jakarta.validation.constraints.NotNull;

public record GroupMemberAddRequest(
        @NotNull(message = "friendId는 필수입니다.")
        Long friendId
) {
}