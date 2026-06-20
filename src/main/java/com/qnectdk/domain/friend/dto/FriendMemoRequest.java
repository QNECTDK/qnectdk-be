package com.qnectdk.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FriendMemoRequest(
        @Schema(description = "메모 대상 친구의 userId", example = "2")
        @NotNull(message = "friendId는 필수입니다.")
        Long friendId,

        @Schema(description = "비공개 메모 내용 (최대 200자)", example = "동아리에서 만난 친구, 사진 좋아함")
        @Size(max = 200, message = "메모는 200자 이내여야 합니다.")
        String content
) {
}