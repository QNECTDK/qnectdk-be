package com.qnectdk.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GroupWithMembersResponse(
        @Schema(description = "생성된 그룹 정보")
        GroupResponse group,

        @Schema(description = "그룹에 추가된 멤버 목록")
        List<GroupMemberResponse> members
) {
}