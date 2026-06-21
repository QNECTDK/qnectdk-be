package com.qnectdk.domain.quiz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 친구 퀴즈 응시로 획득하는 케미 뱃지(F-19). 별도 저장 없이 응시 기록(QuizAttempt)에서 파생 계산한다.
 */
@Getter
@RequiredArgsConstructor
public enum ChemistryBadge {

    FIRST_SOLVE("첫 만남", "친구의 퀴즈를 처음 풀었어요"),
    CHEMISTRY_MASTER("찰떡 케미", "정답률 80% 이상을 달성했어요"),
    PERFECT_MEMORY("완벽 기억", "100% 정답으로 완벽하게 기억했어요");

    private final String label;
    private final String description;
}
