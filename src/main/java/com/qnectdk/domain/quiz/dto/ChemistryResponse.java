package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.quiz.entity.ChemistryBadge;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 나와 특정 친구(owner) 사이의 케미 점수·뱃지(F-07/F-19). 내가 그 친구 퀴즈를 푼 응시 기록에서 파생한다.
 * 케미 점수 = 정답률(%). bestScorePercent(최고)와 latestScorePercent(최근)를 함께 제공한다.
 */
public record ChemistryResponse(
        @Schema(description = "친구(퀴즈 주인) 사용자 ID", example = "34")
        Long ownerId,
        @Schema(description = "내가 이 친구 퀴즈를 응시한 횟수", example = "3")
        int attemptCount,
        @Schema(description = "케미 점수(최고 정답률 %)", example = "80")
        int bestScorePercent,
        @Schema(description = "최근 응시 정답률(%)", example = "60")
        int latestScorePercent,
        @Schema(description = "잠금 해제 여부(한 번이라도 응시했으면 true)", example = "true")
        boolean unlocked,
        @Schema(description = "획득한 케미 뱃지 목록")
        List<BadgeView> badges
) {

    public record BadgeView(
            @Schema(description = "뱃지 코드", example = "PERFECT_MEMORY") String code,
            @Schema(description = "뱃지 이름", example = "완벽 기억") String label,
            @Schema(description = "뱃지 설명", example = "100% 정답으로 완벽하게 기억했어요") String description
    ) {
        public static BadgeView from(ChemistryBadge badge) {
            return new BadgeView(badge.name(), badge.getLabel(), badge.getDescription());
        }
    }

    /** 응시 기록이 없는(아직 안 푼) 친구. */
    public static ChemistryResponse locked(Long ownerId) {
        return new ChemistryResponse(ownerId, 0, 0, 0, false, List.of());
    }

    public static ChemistryResponse of(Long ownerId, int attemptCount, int bestScorePercent,
                                       int latestScorePercent, List<ChemistryBadge> badges) {
        return new ChemistryResponse(
                ownerId, attemptCount, bestScorePercent, latestScorePercent, attemptCount > 0,
                badges.stream().map(BadgeView::from).toList());
    }
}
