package com.qnectdk.domain.daily.dto;

import com.qnectdk.domain.daily.entity.DailyChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 데일리 답변 제출. A 또는 B.
 */
public record DailyAnswerRequest(
        @Schema(description = "선택(A 또는 B)", example = "A")
        @NotNull DailyChoice selected
) {
}
