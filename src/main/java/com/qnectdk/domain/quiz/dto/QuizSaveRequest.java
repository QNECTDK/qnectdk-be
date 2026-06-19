package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.quiz.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 퀴즈 편집·저장 요청(본인). 문항 3~5개. 객관식 보기/정답 정합성은 서비스의 QuizContentValidator 가 검증한다.
 */
public record QuizSaveRequest(
        @Schema(description = "문항 목록(3~5개)")
        @NotNull @Size(min = 3, max = 5) @Valid List<QuestionInput> questions
) {

    public record QuestionInput(
            @Schema(description = "문항 종류(MULTIPLE/OX)")
            @NotNull QuestionType type,
            @Schema(description = "질문", example = "내가 좋아하는 계절은?")
            @NotBlank @Size(max = 255) String content,
            @Schema(description = "정답(OX=O/X, 객관식=정답 보기 텍스트)", example = "O")
            @NotBlank @Size(max = 255) String correctAnswer,
            @Schema(description = "필수 출제 여부", example = "true")
            boolean required,
            @Schema(description = "객관식 보기(OX는 생략/빈 목록)")
            @Valid List<OptionInput> options
    ) {
    }

    public record OptionInput(
            @Schema(description = "보기 텍스트", example = "여름")
            @NotBlank @Size(max = 255) String content,
            @Schema(description = "정답 보기 여부", example = "true")
            boolean correct
    ) {
    }
}
