package com.qnectdk.domain.quiz.service;

import com.qnectdk.domain.quiz.entity.QuestionType;

import java.util.List;

/**
 * 영속화 직전의 퀴즈 콘텐츠 표준형. 수동 저장(요청 DTO)과 자동 생성(GeneratedQuiz)이 모두
 * 이 형태로 변환되어 동일한 검증·저장 경로를 탄다.
 */
public record QuizDraft(List<QuestionDraft> questions) {

    public record QuestionDraft(
            QuestionType type,
            String content,
            String correctAnswer,
            boolean required,
            List<OptionDraft> options
    ) {
    }

    public record OptionDraft(String content, boolean correct) {
    }
}
