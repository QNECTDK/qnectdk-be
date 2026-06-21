package com.qnectdk.domain.reminder.service;

import com.qnectdk.domain.notification.entity.NotificationType;
import com.qnectdk.domain.notification.service.NotificationService;
import com.qnectdk.domain.quiz.service.QuizService;
import com.qnectdk.domain.reminder.entity.QuizReminder;
import com.qnectdk.domain.reminder.repository.QuizReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReminderService {

    private static final int REMIND_AFTER_DAYS = 30;

    private final QuizReminderRepository reminderRepository;
    private final QuizService quizService;
    private final NotificationService notificationService;

    // 친구 수락 시 호출 — ownerId(받을 사람)에게 30일 뒤 리마인드 예약
    @Transactional
    public void scheduleReminder(Long friendshipId, Long ownerId, LocalDateTime acceptedAt) {
      if (reminderRepository.existsByFriendshipIdAndOwnerId(friendshipId, ownerId)) {
            return;
        }
        LocalDateTime scheduledAt = acceptedAt.plusDays(REMIND_AFTER_DAYS);
        reminderRepository.save(QuizReminder.schedule(friendshipId, ownerId, scheduledAt));
    }

    // 발송 1건 처리
    @Transactional
    public void sendOne(Long reminderId) {
      QuizReminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if (reminder == null || reminder.isSent()) {
            return;
        }
        Long ownerId = reminder.getOwnerId();

        Long quizId = quizService.generateReminderQuiz(ownerId).quizId();

        notificationService.push(
                ownerId,
                NotificationType.QUIZ_REMIND,
                "이 사람 기억나세요?",
                "30일 전에 친구가 된 사람의 퀴즈를 다시 풀어보세요!",
                quizId
        );

        reminder.markSent(quizId);
    }

    /**
     * 홈 "이 사람을 기억하나요?" 카드용 — 받는 사람(userId)의 도래한 리마인드의 friendshipId 목록(최신 예정순).
     * sentAt 여부와 무관하게 도래(scheduledAt &lt;= now)한 것을 반환한다(카드는 발송과 별개로 노출 가능).
     */
    public List<Long> findDueFriendshipIds(Long userId) {
      return reminderRepository
          .findByOwnerIdAndScheduledAtLessThanEqualOrderByScheduledAtDesc(userId, LocalDateTime.now())
          .stream()
          .map(QuizReminder::getFriendshipId)
          .toList();
    }
}