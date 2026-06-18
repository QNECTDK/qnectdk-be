package com.qnectdk.domain.reminder.service;

import com.qnectdk.domain.reminder.entity.QuizReminder;
import com.qnectdk.domain.reminder.repository.QuizReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final QuizReminderRepository reminderRepository;
    private final ReminderService reminderService;

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        List<QuizReminder> targets =
                reminderRepository.findByScheduledAtBeforeAndSentAtIsNull(now);

        log.info("[리마인드] 발송 대상 {}건", targets.size());

        for (QuizReminder reminder : targets) {
            try {
                reminderService.sendOne(reminder.getId());
            } catch (Exception e) {
                log.error("[리마인드] 발송 실패 reminderId={}", reminder.getId(), e);
            }
        }
    }
}