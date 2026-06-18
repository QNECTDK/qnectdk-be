package com.qnectdk.domain.quiz.entity;

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
 * 퀴즈 문항. quizId 로 Quiz 를 참조(같은 도메인). 객관식 보기는 {@link QuizOption}.
 * correctAnswer 는 OX = "O"/"X", 객관식 = 정답 보기 텍스트를 담아 채점 기준으로 쓴다.
 */
@Entity
@Table(name = "quiz_questions", indexes = {
        @Index(name = "idx_quiz_questions_quiz", columnList = "quiz_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(name = "correct_answer", nullable = false, length = 255)
    private String correctAnswer;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int seq;

    @Builder
    private QuizQuestion(Long quizId, QuestionType type, String content,
                         String correctAnswer, boolean required, int seq) {
        this.quizId = quizId;
        this.type = type;
        this.content = content;
        this.correctAnswer = correctAnswer;
        this.required = required;
        this.seq = seq;
    }

    public static QuizQuestion create(Long quizId, QuestionType type, String content,
                                      String correctAnswer, boolean required, int seq) {
        return QuizQuestion.builder()
                .quizId(quizId)
                .type(type)
                .content(content)
                .correctAnswer(correctAnswer)
                .required(required)
                .seq(seq)
                .build();
    }
}
