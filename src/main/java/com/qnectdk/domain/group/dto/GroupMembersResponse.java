package com.qnectdk.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GroupMembersResponse(
        @Schema(description = "그룹 id", example = "1")
        Long groupId,

        @Schema(description = "그룹명", example = "24학번동기")
        String name,

        @Schema(description = "그룹 멤버 목록")
        List<GroupMemberCardResponse> members
) {
}
