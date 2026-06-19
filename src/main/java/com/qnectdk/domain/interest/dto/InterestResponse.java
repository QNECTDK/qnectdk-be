package com.qnectdk.domain.interest.dto;

import com.qnectdk.domain.interest.entity.Interest;
import io.swagger.v3.oas.annotations.media.Schema;

public record InterestResponse(
        @Schema(description = "관심사 ID", example = "1") Long id,
        @Schema(description = "관심사 카테고리명", example = "운동") String category,
        @Schema(description = "관심사 이름", example = "헬스") String name
) {

    public static InterestResponse from(Interest interest) {
        return new InterestResponse(interest.getId(), interest.getCategory(), interest.getName());
    }
}
