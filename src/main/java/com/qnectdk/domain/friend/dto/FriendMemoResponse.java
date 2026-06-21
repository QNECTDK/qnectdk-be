package com.qnectdk.domain.friend.dto;

import com.qnectdk.domain.friend.entity.FriendMemo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FriendMemoResponse(
        @Schema(description = "메모 id", example = "1")
        Long memoId,

        @Schema(description = "메모를 작성한 사람(나)의 userId", example = "1")
        Long ownerId,

        @Schema(description = "메모 대상 친구의 userId", example = "2")
        Long friendId,

        @Schema(description = "비공개 메모 내용", example = "동아리에서 만난 친구, 사진 좋아함")
        String content,

        @Schema(description = "메모 마지막 수정 시각", example = "2026-06-19T14:28:36")
        LocalDateTime updatedAt
) {
    public static FriendMemoResponse from(FriendMemo m) {
        return new FriendMemoResponse(
                m.getId(),
                m.getOwnerId(),
                m.getFriendId(),
                m.getContent(),
                m.getUpdatedAt()
        );
    }

    /** 아직 작성된 메모가 없을 때(정상 상태) 빈 응답. */
    public static FriendMemoResponse empty(Long ownerId, Long friendId) {
      return new FriendMemoResponse(null, ownerId, friendId, null, null);
    }
}