package com.qnectdk.domain.group.dto;

import com.qnectdk.domain.group.entity.FriendGroupMember;
import io.swagger.v3.oas.annotations.media.Schema;


public record GroupMemberResponse(
        @Schema(description = "그룹 멤버 id", example = "1")
        Long memberId,

        @Schema(description = "그룹 id", example = "1")
        Long groupId,

        @Schema(description = "멤버로 추가된 친구의 userId", example = "2")
        Long friendId
) {
    public static GroupMemberResponse from(FriendGroupMember m) {
        return new GroupMemberResponse(
                m.getId(),
                m.getGroupId(),
                m.getFriendId()
        );
    }
}