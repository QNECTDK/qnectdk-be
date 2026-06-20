package com.qnectdk.domain.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PointBalanceResponse(
        @Schema(description = "현재 포인트 잔액", example = "300")
        int balance
) {
}