package com.qnectdk.domain.notification.service;

import com.qnectdk.domain.notification.dto.NotificationResponse;
import com.qnectdk.domain.notification.entity.Notification;
import com.qnectdk.domain.notification.entity.NotificationType;
import com.qnectdk.domain.notification.repository.NotificationRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ===== A·스케줄러에게 제공하는 함수 (push) =====
    // 알림 한 줄 생성만. 묶지 않음(한 명당 한 줄).
    @Transactional
    public void push(Long userId, NotificationType type, String title, String body, Long refId) {
        notificationRepository.save(Notification.create(userId, type, title, body, refId));
    }

    // 내 알림 목록 (최신순)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    // 안 읽은 개수 (벨 배지)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // 알림 하나 읽음 처리
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT)); // 알림 없음(공통 매핑)
        if (!n.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED); // 남의 알림
        }
        n.markAsRead();
    }
}