package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.service.QuizDraft;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * AI가 생성한 문제 초안(저장되지 않음). 클라이언트가 "내 퀴즈 목록"에 추가한 뒤
 * 편집/취사선택해 {@code PUT /api/quizzes/me}로 저장한다. 서버는 기존 퀴즈를 덮어쓰지 않는다.
 */
public record AiQuizDraftResponse(
        @Schema(description = "AI 생성 문제 초안 목록(미저장)") List<DraftQuestion> questions
) {

    public record DraftQuestion(
            @Schema(description = "문항 종류(MULTIPLE/OX)") QuestionType type,
            @Schema(description = "질문") String content,
            @Schema(description = "정답(OX=O/X, 객관식=정답 보기 텍스트)") String correctAnswer,
            @Schema(description = "필수 출제 여부") boolean required,
            @Schema(description = "객관식 보기(2~4개, OX는 빈 목록)") List<DraftOption> options
    ) {
    }

    public record DraftOption(
            @Schema(description = "보기 텍스트") String content,
            @Schema(description = "정답 보기 여부") boolean correct
    ) {
    }

    public static AiQuizDraftResponse from(QuizDraft draft) {
        List<DraftQuestion> questions = draft.questions().stream()
                .map(question -> new DraftQuestion(
                        question.type(),
                        question.content(),
                        question.correctAnswer(),
                        question.required(),
                        question.options().stream()
                                .map(option -> new DraftOption(option.content(), option.correct()))
                                .toList()))
                .toList();
        return new AiQuizDraftResponse(questions);
    }
}
