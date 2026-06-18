package com.qnectdk.domain.point.repository;

import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndReasonAndRefId(Long userId, PointReason reason, Long refId);
}