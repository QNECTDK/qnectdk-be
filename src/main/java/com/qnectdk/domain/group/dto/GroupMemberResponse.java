package com.qnectdk.domain.group.dto;

import com.qnectdk.domain.group.entity.FriendGroupMember;


public record GroupMemberResponse(
        Long memberId,
        Long groupId,
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