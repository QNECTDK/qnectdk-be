package com.qnectdk.domain.quiz.client;

import com.qnectdk.domain.quiz.client.dto.GeneratedQuiz;
import com.qnectdk.domain.quiz.client.dto.QuizGenerationCommand;

/**
 * 퀴즈 자동 생성 추상화. 구현은 모델 교체·목 테스트를 위해 빈으로 주입된다.
 * 기본은 {@link MockQuizGenerationClient}, app.gemini.enabled=true 면 {@link GeminiQuizGenerationClient}.
 */
public interface QuizGenerationClient {

    GeneratedQuiz generate(QuizGenerationCommand command);
}
