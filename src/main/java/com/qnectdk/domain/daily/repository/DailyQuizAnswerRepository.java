package com.qnectdk.domain.daily.repository;

import com.qnectdk.domain.daily.entity.DailyChoice;
import com.qnectdk.domain.daily.entity.DailyQuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DailyQuizAnswerRepository extends JpaRepository<DailyQuizAnswer, Long> {

    Optional<DailyQuizAnswer> findByDailyQuizIdAndUserId(Long dailyQuizId, Long userId);

    boolean existsByDailyQuizIdAndUserId(Long dailyQuizId, Long userId);

    // 전체 비율 집계용 (선택지별 카운트).
    long countByDailyQuizIdAndSelected(Long dailyQuizId, DailyChoice selected);

    // 친구 비율/개별 선택 표시용 — 친구 userId 목록으로 필터.
    List<DailyQuizAnswer> findByDailyQuizIdAndUserIdIn(Long dailyQuizId, List<Long> userIds);
}
