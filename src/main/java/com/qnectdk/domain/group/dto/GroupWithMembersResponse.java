package com.qnectdk.domain.group.dto;

import java.util.List;

public record GroupWithMembersResponse(
        GroupResponse group,
        List<GroupMemberResponse> members
) {
}