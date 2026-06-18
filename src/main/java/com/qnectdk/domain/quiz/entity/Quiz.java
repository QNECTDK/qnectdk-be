package com.qnectdk.domain.quiz.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 세트. owner_id(출제 대상 사용자)별로 항상 하나만 active 하다.
 * 새로 생성(첫 만남/리마인드)하면 이전 active 퀴즈는 비활성화된다 — active 퀴즈가 친구가 푸는 대상.
 */
@Entity
@Table(name = "quizzes", indexes = {
        @Index(name = "idx_quizzes_owner_type", columnList = "owner_id, type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizType type;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    private Quiz(Long ownerId, QuizType type, boolean active) {
        this.ownerId = ownerId;
        this.type = type;
        this.active = active;
    }

    public static Quiz create(Long ownerId, QuizType type) {
        return Quiz.builder()
                .ownerId(ownerId)
                .type(type)
                .active(true)
                .build();
    }

    public void deactivate() {
        this.active = false;
    }
}
