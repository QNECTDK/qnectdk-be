package com.qnectdk.domain.point.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_transactions", indexes = {
        @Index(name = "idx_point_tx_user", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointReason reason;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Builder
    private PointTransaction(Long userId, int amount, PointReason reason, Long refId, int balanceAfter) {
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.refId = refId;
        this.balanceAfter = balanceAfter;
    }

    public static PointTransaction of(Long userId, int amount, PointReason reason, Long refId, int balanceAfter) {
        return PointTransaction.builder()
                .userId(userId)
                .amount(amount)
                .reason(reason)
                .refId(refId)
                .balanceAfter(balanceAfter)
                .build();
    }
}