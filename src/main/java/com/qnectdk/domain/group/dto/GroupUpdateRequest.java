package com.qnectdk.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupUpdateRequest(
        @Schema(description = "수정할 그룹명 (사용자별 중복 불가, 최대 30자)", example = "25학번동기")
        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 30, message = "그룹명은 30자 이내여야 합니다.")
        String name,

        @Schema(description = "수정할 해시태그 (통짜 텍스트, 최대 100자)", example = "#동아리 #여행")
        @Size(max = 100, message = "해시태그는 100자 이내여야 합니다.")
        String hashtags
) {
}