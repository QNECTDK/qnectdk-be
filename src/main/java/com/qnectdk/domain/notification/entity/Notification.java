package com.qnectdk.domain.notification.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_noti_user_read", columnList = "user_id, is_read")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 받는 사람

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 255)
    private String body;

    @Column(name = "ref_id")
    private Long refId; // 연결 대상 id (친구 id 또는 퀴즈 id)

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Builder
    private Notification(Long userId, NotificationType type, String title, String body, Long refId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.refId = refId;
        this.isRead = false; // 생성 시 항상 안 읽음
    }

    public static Notification create(Long userId, NotificationType type, String title, String body, Long refId) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .refId(refId)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}