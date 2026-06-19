package com.qnectdk.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record GroupMemberAddRequest(
        @Schema(description = "그룹에 추가할 친구의 userId (ACCEPTED 친구만 가능)", example = "2")
        @NotNull(message = "friendId는 필수입니다.")
        Long friendId
) {
}