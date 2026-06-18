package com.qnectdk.domain.daily.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 하루 1개의 데일리 밸런스 게임. 밸런스 전용이라 옵션 테이블 없이 A/B 두 컬럼만 둔다.
 */
@Entity
@Table(name = "daily_quizzes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_quizzes_date", columnNames = "quiz_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuiz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_date", nullable = false, unique = true)
    private LocalDate quizDate;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(name = "option_a", nullable = false, length = 100)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 100)
    private String optionB;

    @Builder
    private DailyQuiz(LocalDate quizDate, String content, String optionA, String optionB) {
        this.quizDate = quizDate;
        this.content = content;
        this.optionA = optionA;
        this.optionB = optionB;
    }

    public static DailyQuiz create(LocalDate quizDate, String content, String optionA, String optionB) {
        return DailyQuiz.builder()
                .quizDate(quizDate)
                .content(content)
                .optionA(optionA)
                .optionB(optionB)
                .build();
    }
}
