package com.qnectdk.domain.quiz.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 퀴즈 응시 기록. 정답률(score/total) 이 케미 점수의 근거가 된다.
 */
@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_quiz_attempts_solver_quiz", columnList = "solver_id, quiz_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "solver_id", nullable = false)
    private Long solverId;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int total;

    @Builder
    private QuizAttempt(Long quizId, Long solverId, int score, int total) {
        this.quizId = quizId;
        this.solverId = solverId;
        this.score = score;
        this.total = total;
    }

    public static QuizAttempt create(Long quizId, Long solverId, int score, int total) {
        return QuizAttempt.builder()
                .quizId(quizId)
                .solverId(solverId)
                .score(score)
                .total(total)
                .build();
    }
}
