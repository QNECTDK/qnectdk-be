package com.qnectdk.domain.quiz.client.dto;

import com.qnectdk.domain.quiz.entity.QuestionType;

import java.util.List;

/**
 * 퀴즈 생성기(Gemini/Mock)의 출력. 모델 JSON 응답이 이 스키마로 역직렬화된다.
 * 콘텐츠 규칙 검증(문항 수, 보기/정답 정합성)은 영속화 직전 서비스 계층에서 수행한다.
 */
public record GeneratedQuiz(List<GeneratedQuestion> questions) {

    public record GeneratedQuestion(
            QuestionType type,
            String content,
            String correctAnswer,
            boolean required,
            List<GeneratedOption> options
    ) {
    }

    public record GeneratedOption(String content, boolean correct) {
    }
}
