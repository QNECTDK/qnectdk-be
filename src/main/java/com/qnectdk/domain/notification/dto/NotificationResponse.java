package com.qnectdk.domain.notification.dto;

import com.qnectdk.domain.notification.entity.Notification;
import com.qnectdk.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        String title,
        String body,
        Long refId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getRefId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}