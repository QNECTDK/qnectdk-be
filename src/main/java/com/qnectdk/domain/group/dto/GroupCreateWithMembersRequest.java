package com.qnectdk.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GroupCreateWithMembersRequest(
        @Schema(description = "그룹명 (사용자별 중복 불가, 최대 30자)", example = "24학번동기")
        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 30, message = "그룹명은 30자 이내여야 합니다.")
        String name,

        @Schema(description = "해시태그 목록 (# 없이)", example = "[\"동아리\", \"MT\"]")
        List<String> hashtags,

        // 그룹 생성과 동시에 추가할 친구 id 목록 (비어있어도 됨 = 멤버 없이 그룹만 생성)
        @Schema(description = "그룹 생성과 동시에 추가할 친구 userId 목록 (비어있으면 멤버 없이 생성)", example = "[2, 3]")
        List<Long> friendIds
) {
}