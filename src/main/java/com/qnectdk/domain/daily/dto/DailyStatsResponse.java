package com.qnectdk.domain.daily.dto;

import com.qnectdk.domain.daily.entity.DailyChoice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 데일리 통계. 내가 답한 뒤에만 공개된다. 전체 비율 + 친구 비율 + 친구별 개별 선택.
 */
public record DailyStatsResponse(
        @Schema(description = "데일리 퀴즈 ID", example = "1") Long dailyQuizId,
        @Schema(description = "전체 사용자 비율") Distribution overall,
        @Schema(description = "내 친구 비율 + 개별 선택") FriendStats friends
) {

    public record Distribution(
            @Schema(description = "A 선택 수", example = "120") long countA,
            @Schema(description = "B 선택 수", example = "80") long countB,
            @Schema(description = "응답 합계", example = "200") long total,
            @Schema(description = "A 비율(%)", example = "60") int percentA,
            @Schema(description = "B 비율(%)", example = "40") int percentB
    ) {
        public static Distribution of(long countA, long countB) {
            long total = countA + countB;
            return new Distribution(countA, countB, total, percent(countA, total), percent(countB, total));
        }

        private static int percent(long part, long total) {
            return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
        }
    }

    public record FriendStats(
            @Schema(description = "친구 A 선택 수", example = "3") long countA,
            @Schema(description = "친구 B 선택 수", example = "2") long countB,
            @Schema(description = "응답한 친구 합계", example = "5") long total,
            @Schema(description = "친구 A 비율(%)", example = "60") int percentA,
            @Schema(description = "친구 B 비율(%)", example = "40") int percentB,
            @Schema(description = "친구별 개별 선택(답한 친구만)") List<FriendChoice> selections
    ) {
    }

    public record FriendChoice(
            @Schema(description = "친구 사용자 ID", example = "2") Long userId,
            @Schema(description = "친구 이름", example = "이영희") String name,
        @Schema(description = "친구 캐릭터 식별자(아바타 표시용)", example = "character07") String characterId,
                @Schema(description = "친구의 선택") DailyChoice selected
    ) {
    }
}
