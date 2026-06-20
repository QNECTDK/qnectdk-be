package com.qnectdk.domain.notification.dto;

import com.qnectdk.domain.notification.entity.Notification;
import com.qnectdk.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(description = "알림 id", example = "1")
        Long notificationId,

        @Schema(description = "알림 종류")
        NotificationType type,

        @Schema(description = "알림 제목", example = "새로운 친구가 1명 추가되었습니다!")
        String title,

        @Schema(description = "알림 본문", example = "친구 요청이 수락되었어요.")
        String body,

        @Schema(description = "알림 종류별 참조 id (예: 친구 userId, 퀴즈 id)", example = "2")
        Long refId,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "알림 생성 시각", example = "2026-06-19T14:28:36")
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