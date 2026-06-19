package com.qnectdk.domain.reminder.repository;

import com.qnectdk.domain.reminder.entity.QuizReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizReminderRepository extends JpaRepository<QuizReminder, Long> {

    List<QuizReminder> findByScheduledAtBeforeAndSentAtIsNull(LocalDateTime now);

    // 이 친구 관계 + 받는 사람 조합으로 이미 예약됐는지
    boolean existsByFriendshipIdAndOwnerId(Long friendshipId, Long ownerId);
}