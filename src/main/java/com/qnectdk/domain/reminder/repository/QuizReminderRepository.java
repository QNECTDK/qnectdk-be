package com.qnectdk.domain.reminder.repository;

import com.qnectdk.domain.reminder.entity.QuizReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizReminderRepository extends JpaRepository<QuizReminder, Long> {

    List<QuizReminder> findByScheduledAtBeforeAndSentAtIsNull(LocalDateTime now);

    // 이 친구 관계 + 받는 사람 조합으로 이미 예약됐는지
    boolean existsByFriendshipIdAndOwnerId(Long friendshipId, Long ownerId);

    // 홈 "이 사람을 기억하나요?" 카드용 — 받는 사람(ownerId)의 도래한 리마인드를 최신 예정순으로.
    List<QuizReminder> findByOwnerIdAndScheduledAtLessThanEqualOrderByScheduledAtDesc(
        Long ownerId, LocalDateTime now);
}