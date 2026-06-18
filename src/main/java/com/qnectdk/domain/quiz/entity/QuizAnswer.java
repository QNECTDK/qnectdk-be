package com.qnectdk.domain.quiz.entity;

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
 * 응시 문항별 답안. 결과 화면(SCR-014)의 문항별 정/오답 표시에 사용한다.
 */
@Entity
@Table(name = "quiz_answers", indexes = {
        @Index(name = "idx_quiz_answers_attempt", columnList = "attempt_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 255)
    private String answer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Builder
    private QuizAnswer(Long attemptId, Long questionId, String answer, boolean correct) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answer = answer;
        this.correct = correct;
    }

    public static QuizAnswer create(Long attemptId, Long questionId, String answer, boolean correct) {
        return QuizAnswer.builder()
                .attemptId(attemptId)
                .questionId(questionId)
                .answer(answer)
                .correct(correct)
                .build();
    }
}
