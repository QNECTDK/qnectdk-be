package com.qnectdk.domain.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GroupCreateWithMembersRequest(
        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 30, message = "그룹명은 30자 이내여야 합니다.")
        String name,

        @Size(max = 100, message = "해시태그는 100자 이내여야 합니다.")
        String hashtags,

        // 그룹 생성과 동시에 추가할 친구 id 목록 (비어있어도 됨 = 멤버 없이 그룹만 생성)
        List<Long> friendIds
) {
}