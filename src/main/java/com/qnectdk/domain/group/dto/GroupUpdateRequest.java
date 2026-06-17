package com.qnectdk.domain.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupUpdateRequest(
        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 30, message = "그룹명은 30자 이내여야 합니다.")
        String name,

        @Size(max = 100, message = "해시태그는 100자 이내여야 합니다.")
        String hashtags
) {
}