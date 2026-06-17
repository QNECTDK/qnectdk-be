package com.qnectdk.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 채점 결과. score/total(정답률)이 케미 점수의 근거가 되고, 문항별 정/오답은 결과 화면(SCR-014)에 쓰인다.
 */
public record QuizResultResponse(
        @Schema(description = "응시 ID") Long attemptId,
        @Schema(description = "퀴즈 ID") Long quizId,
        @Schema(description = "맞힌 개수") int score,
        @Schema(description = "총 문항 수") int total,
        @Schema(description = "문항별 채점 결과") List<AnswerResult> answers
) {

    public record AnswerResult(
            @Schema(description = "문항 ID") Long questionId,
            @Schema(description = "질문") String content,
            @Schema(description = "내가 고른 답") String yourAnswer,
            @Schema(description = "정답") String correctAnswer,
            @Schema(description = "정답 여부") boolean correct
    ) {
    }
}
