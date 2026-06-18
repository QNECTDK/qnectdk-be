package com.qnectdk.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 퀴즈 응시(제출) 요청. 문항별로 고른 답을 담는다(객관식=보기 텍스트, OX=O/X).
 */
public record QuizAttemptRequest(
        @Schema(description = "문항별 답안(최대 문항 수만큼)")
        @NotNull @Size(min = 1, max = 5) @Valid List<AnswerInput> answers
) {

    public record AnswerInput(
            @Schema(description = "문항 ID")
            @NotNull Long questionId,
            @Schema(description = "고른 답(객관식=보기 텍스트, OX=O/X)")
            @NotBlank @Size(max = 255) String answer
    ) {
    }
}
