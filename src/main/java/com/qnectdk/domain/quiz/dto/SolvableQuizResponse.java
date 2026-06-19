package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizOption;
import com.qnectdk.domain.quiz.entity.QuizQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 친구가 푸는 화면용 퀴즈. 정답·정답보기는 노출하지 않는다.
 * solvable=false 면 응시 불가(프로필 미완성) — 열람만 가능.
 */
public record SolvableQuizResponse(
        @Schema(description = "퀴즈 ID", example = "1") Long quizId,
        @Schema(description = "퀴즈 주인 사용자 ID", example = "1") Long ownerId,
        @Schema(description = "퀴즈 주인 이름", example = "홍길동") String ownerName,
        @Schema(description = "응시 가능 여부(false=프로필 미완성, 열람만)", example = "true") boolean solvable,
        @Schema(description = "문항 목록(정답 미포함)") List<Question> questions
) {

    public record Question(
            @Schema(description = "문항 ID", example = "1") Long questionId,
            @Schema(description = "문항 종류") QuestionType type,
            @Schema(description = "질문", example = "내가 좋아하는 계절은?") String content,
            @Schema(description = "문항 순서", example = "1") int seq,
            @Schema(description = "객관식 보기(OX는 빈 목록)") List<Option> options
    ) {
    }

    public record Option(
            @Schema(description = "보기 ID", example = "1") Long optionId,
            @Schema(description = "보기 텍스트", example = "여름") String content,
            @Schema(description = "보기 순서", example = "1") int seq
    ) {
    }

    public static SolvableQuizResponse of(Quiz quiz, String ownerName, boolean solvable,
                                          List<QuizQuestion> questions,
                                          Map<Long, List<QuizOption>> optionsByQuestionId) {
        List<Question> views = questions.stream()
                .map(question -> new Question(
                        question.getId(),
                        question.getType(),
                        question.getContent(),
                        question.getSeq(),
                        optionsByQuestionId.getOrDefault(question.getId(), List.of()).stream()
                                .map(option -> new Option(option.getId(), option.getContent(), option.getSeq()))
                                .toList()))
                .toList();
        return new SolvableQuizResponse(quiz.getId(), quiz.getOwnerId(), ownerName, solvable, views);
    }
}
