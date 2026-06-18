package com.qnectdk.domain.reminder.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_reminders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reminder_friendship_owner", columnNames = {"friendship_id", "owner_id"})
}, indexes = {
        @Index(name = "idx_reminder_scheduled", columnList = "scheduled_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizReminder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "friendship_id", nullable = false)
    private Long friendshipId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // 리마인드 받을 사람

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Builder
    private QuizReminder(Long friendshipId, Long ownerId, LocalDateTime scheduledAt) {
        this.friendshipId = friendshipId;
        this.ownerId = ownerId;
        this.scheduledAt = scheduledAt;
    }

    public static QuizReminder schedule(Long friendshipId, Long ownerId, LocalDateTime scheduledAt) {
        return QuizReminder.builder()
                .friendshipId(friendshipId)
                .ownerId(ownerId)
                .scheduledAt(scheduledAt)
                .build();
    }

    public void markSent(Long quizId) {
        this.quizId = quizId;
        this.sentAt = LocalDateTime.now();
    }

    public boolean isSent() {
        return this.sentAt != null;
    }
}