package com.qnectdk.domain.point.service;

import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.quiz.port.PointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointPortImpl implements PointPort {

    private final PointService pointService;

    @Override
    public void earnQuizFirstSolve(Long solverId, Long ownerId, Long quizId) {
        // 친구(ownerId) 1명당 1회만 적립 — 멱등 키는 refId=ownerId
        pointService.earn(
                solverId,
                PointPolicy.QUIZ_FIRST_SOLVE,
                PointReason.QUIZ_FIRST_SOLVE,
                ownerId   // refId = ownerId (친구당 1회 멱등)
        );
    }
}