package com.qnectdk.domain.quiz.client.dto;

import com.qnectdk.domain.quiz.entity.QuizType;

import java.util.List;

/**
 * 퀴즈 자동 생성 입력. 출제 대상(owner)의 프로필 스냅샷 + 생성 옵션.
 * 실제로 존재하는 프로필 필드만 담는다(없으면 null). 호출부에서 조립한다.
 */
public record QuizGenerationCommand(
        String ownerName,
        String school,
        String gender,
        String mbti,
        String drinkLevel,
        String favoriteFood,
        List<String> interests,
        QuizType type,
        int minQuestions,
        int maxQuestions
) {
}
