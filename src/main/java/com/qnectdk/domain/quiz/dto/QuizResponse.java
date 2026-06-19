package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizOption;
import com.qnectdk.domain.quiz.entity.QuizQuestion;
import com.qnectdk.domain.quiz.entity.QuizType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 본인(출제자) 편집용 퀴즈 응답. 정답·정답보기를 포함한다(본인만 보는 화면).
 */
public record QuizResponse(
        @Schema(description = "퀴즈 ID", example = "1") Long quizId,
        @Schema(description = "퀴즈 종류") QuizType type,
        @Schema(description = "활성 여부", example = "true") boolean active,
        @Schema(description = "문항 목록(정답 포함)") List<QuestionView> questions
) {

    public record QuestionView(
            @Schema(description = "문항 ID", example = "1") Long questionId,
            @Schema(description = "문항 종류") QuestionType type,
            @Schema(description = "질문", example = "내가 좋아하는 계절은?") String content,
            @Schema(description = "정답(OX=O/X, 객관식=정답 보기 텍스트)", example = "O") String correctAnswer,
            @Schema(description = "필수 출제 여부", example = "true") boolean required,
            @Schema(description = "문항 순서", example = "1") int seq,
            @Schema(description = "객관식 보기(OX는 빈 목록)") List<OptionView> options
    ) {
    }

    public record OptionView(
            @Schema(description = "보기 ID", example = "1") Long optionId,
            @Schema(description = "보기 텍스트", example = "여름") String content,
            @Schema(description = "정답 보기 여부", example = "true") boolean correct,
            @Schema(description = "보기 순서", example = "1") int seq
    ) {
    }

    public static QuizResponse of(Quiz quiz, List<QuizQuestion> questions,
                                  Map<Long, List<QuizOption>> optionsByQuestionId) {
        List<QuestionView> views = questions.stream()
                .map(question -> new QuestionView(
                        question.getId(),
                        question.getType(),
                        question.getContent(),
                        question.getCorrectAnswer(),
                        question.isRequired(),
                        question.getSeq(),
                        optionsByQuestionId.getOrDefault(question.getId(), List.of()).stream()
                                .map(option -> new OptionView(
                                        option.getId(), option.getContent(), option.isCorrect(), option.getSeq()))
                                .toList()))
                .toList();
        return new QuizResponse(quiz.getId(), quiz.getType(), quiz.isActive(), views);
    }
}
