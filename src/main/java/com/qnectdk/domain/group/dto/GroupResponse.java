package com.qnectdk.domain.group.dto;

import com.qnectdk.domain.group.entity.FriendGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GroupResponse(
        @Schema(description = "그룹 id", example = "1")
        Long groupId,

        @Schema(description = "그룹을 만든 사람의 userId", example = "1")
        Long userId,

        @Schema(description = "그룹명", example = "24학번동기")
        String name,

        @Schema(description = "해시태그 (통짜 텍스트)", example = "#동아리 #MT")
        String hashtags,

        @Schema(description = "그룹 생성 시각", example = "2026-06-19T14:28:36")
        LocalDateTime createdAt
) {
    public static GroupResponse from(FriendGroup g) {
        return new GroupResponse(
                g.getId(),
                g.getUserId(),
                g.getName(),
                g.getHashtags(),
                g.getCreatedAt()
        );
    }
}