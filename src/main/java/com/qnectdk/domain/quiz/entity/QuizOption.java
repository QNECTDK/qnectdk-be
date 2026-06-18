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
 * 객관식(MULTIPLE) 보기. questionId 로 {@link QuizQuestion} 을 참조(같은 도메인).
 * OX 문항은 보기 없이 사용한다.
 */
@Entity
@Table(name = "quiz_options", indexes = {
        @Index(name = "idx_quiz_options_question", columnList = "question_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int seq;

    @Builder
    private QuizOption(Long questionId, String content, boolean correct, int seq) {
        this.questionId = questionId;
        this.content = content;
        this.correct = correct;
        this.seq = seq;
    }

    public static QuizOption create(Long questionId, String content, boolean correct, int seq) {
        return QuizOption.builder()
                .questionId(questionId)
                .content(content)
                .correct(correct)
                .seq(seq)
                .build();
    }
}
