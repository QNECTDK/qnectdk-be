package com.qnectdk.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 채점 결과. score/total(정답률)이 케미 점수의 근거가 되고, 문항별 정/오답은 결과 화면(SCR-014)에 쓰인다.
 */
public record QuizResultResponse(
        @Schema(description = "응시 ID", example = "1") Long attemptId,
        @Schema(description = "퀴즈 ID", example = "1") Long quizId,
        @Schema(description = "맞힌 개수", example = "3") int score,
        @Schema(description = "총 문항 수", example = "5") int total,
    @Schema(description = "정답률(%) — 케미 점수 근거", example = "60") int scorePercent,
            @Schema(description = "문항별 채점 결과") List<AnswerResult> answers
) {

  public static QuizResultResponse of(Long attemptId, Long quizId, int score, int total, List<AnswerResult> answers) {
    int percent = total == 0 ? 0 : (int) Math.round(score * 100.0 / total);
    return new QuizResultResponse(attemptId, quizId, score, total, percent, answers);
  }

    public record AnswerResult(
            @Schema(description = "문항 ID", example = "1") Long questionId,
            @Schema(description = "질문", example = "내가 좋아하는 계절은?") String content,
            @Schema(description = "내가 고른 답", example = "O") String yourAnswer,
            @Schema(description = "정답", example = "O") String correctAnswer,
            @Schema(description = "정답 여부", example = "true") boolean correct
    ) {
    }
}
