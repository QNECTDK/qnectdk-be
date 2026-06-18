package com.qnectdk.domain.daily.repository;

import com.qnectdk.domain.daily.entity.DailyQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyQuizRepository extends JpaRepository<DailyQuiz, Long> {

    Optional<DailyQuiz> findByQuizDate(LocalDate quizDate);
}
