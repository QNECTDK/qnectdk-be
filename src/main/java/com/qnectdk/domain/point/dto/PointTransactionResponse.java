package com.qnectdk.domain.point.dto;

import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.entity.PointTransaction;

import java.time.LocalDateTime;

public record PointTransactionResponse(
        Long transactionId,
        int amount,
        PointReason reason,
        Long refId,
        int balanceAfter,
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