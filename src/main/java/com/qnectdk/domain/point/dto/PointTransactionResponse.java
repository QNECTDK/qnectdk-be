package com.qnectdk.domain.point.dto;

import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.entity.PointTransaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PointTransactionResponse(
        @Schema(description = "거래 내역 id", example = "1")
        Long transactionId,

        @Schema(description = "적립/차감 양 (적립 +, 차감 -)", example = "5")
        int amount,

        @Schema(description = "적립/차감 사유")
        PointReason reason,

        @Schema(description = "사유와 연관된 참조 id (예: 그룹 id, 상점 아이템 id). 없으면 null", example = "1")
        Long refId,

        @Schema(description = "이 거래 직후의 잔액", example = "300")
        int balanceAfter,

        @Schema(description = "거래 발생 시각", example = "2026-06-19T14:28:36")
        LocalDateTime createdAt
) {
    public static PointTransactionResponse from(PointTransaction tx) {
        return new PointTransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getReason(),
                tx.getRefId(),
                tx.getBalanceAfter(),
                tx.getCreatedAt()
        );
    }
}