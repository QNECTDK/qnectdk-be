package com.qnectdk.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record InterestCategoryResponse(
        @Schema(description = "관심사 카테고리명", example = "운동") String category,
        @Schema(description = "해당 카테고리에 속한 관심사 목록") List<InterestItem> interests
) {
}
