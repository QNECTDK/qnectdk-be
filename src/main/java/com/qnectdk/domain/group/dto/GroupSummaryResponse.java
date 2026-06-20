package com.qnectdk.domain.group.dto;

import com.qnectdk.domain.group.entity.FriendGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record GroupSummaryResponse(
        @Schema(description = "그룹 id", example = "1")
        Long groupId,

        @Schema(description = "그룹명", example = "24학번동기")
        String name,

        @Schema(description = "해시태그 목록 (# 제거)", example = "[\"동아리\", \"MT\"]")
        List<String> hashtags,

        @Schema(description = "그룹 멤버 수", example = "4")
        int memberCount,

        @Schema(description = "그룹 생성 시각", example = "2026-06-19T14:28:36")
        LocalDateTime createdAt
) {
    public static GroupSummaryResponse from(FriendGroup g, int memberCount) {
        return new GroupSummaryResponse(
                g.getId(),
                g.getName(),
                splitHashtags(g.getHashtags()),
                memberCount,
                g.getCreatedAt()
        );
    }

    private static List<String> splitHashtags(String hashtags) {
        if (hashtags == null || hashtags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(hashtags.trim().split("\\s+"))
                .map(tag -> tag.startsWith("#") ? tag.substring(1) : tag)
                .filter(tag -> !tag.isBlank())
                .toList();
    }
}
