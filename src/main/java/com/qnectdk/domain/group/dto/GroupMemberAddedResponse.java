package com.qnectdk.domain.group.dto;

import com.qnectdk.domain.profile.dto.PersonCard;
import io.swagger.v3.oas.annotations.media.Schema;

public record GroupMemberAddedResponse(
        @Schema(description = "그룹 멤버 id", example = "1")
        Long memberId,

        @Schema(description = "그룹 id", example = "1")
        Long groupId,

        @Schema(description = "추가된 멤버의 person 카드")
        PersonCard person
) {
}
