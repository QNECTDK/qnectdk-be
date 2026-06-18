package com.qnectdk.domain.daily.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 데일리 답변. (daily_quiz_id, user_id) 유니크로 1인 1회를 보장한다.
 */
@Entity
@Table(name = "daily_quiz_answers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_quiz_answers_quiz_user", columnNames = {"daily_quiz_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuizAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_quiz_id", nullable = false)
    private Long dailyQuizId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private DailyChoice selected;

    @Builder
    private DailyQuizAnswer(Long dailyQuizId, Long userId, DailyChoice selected) {
        this.dailyQuizId = dailyQuizId;
        this.userId = userId;
        this.selected = selected;
    }

    public static DailyQuizAnswer create(Long dailyQuizId, Long userId, DailyChoice selected) {
        return DailyQuizAnswer.builder()
                .dailyQuizId(dailyQuizId)
                .userId(userId)
                .selected(selected)
                .build();
    }
}
