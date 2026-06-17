package com.qnectdk.domain.daily.dto;

import com.qnectdk.domain.daily.entity.DailyChoice;
import com.qnectdk.domain.daily.entity.DailyQuiz;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 오늘의 데일리 질문 + 내 답변 상태. 통계는 별도 조회(내가 답해야 공개)로 분리한다.
 */
public record DailyTodayResponse(
        @Schema(description = "데일리 퀴즈 ID") Long dailyQuizId,
        @Schema(description = "출제 날짜") LocalDate quizDate,
        @Schema(description = "질문") String content,
        @Schema(description = "선택지 A") String optionA,
        @Schema(description = "선택지 B") String optionB,
        @Schema(description = "내가 답했는지 여부") boolean answered,
        @Schema(description = "내 선택(미답변 시 null)") DailyChoice mySelection
) {

    public static DailyTodayResponse answered(DailyQuiz quiz, DailyChoice selected) {
        return new DailyTodayResponse(quiz.getId(), quiz.getQuizDate(), quiz.getContent(),
                quiz.getOptionA(), quiz.getOptionB(), true, selected);
    }

    public static DailyTodayResponse notAnswered(DailyQuiz quiz) {
        return new DailyTodayResponse(quiz.getId(), quiz.getQuizDate(), quiz.getContent(),
                quiz.getOptionA(), quiz.getOptionB(), false, null);
    }
}
