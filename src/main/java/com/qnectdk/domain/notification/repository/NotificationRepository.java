package com.qnectdk.domain.notification.repository;

import com.qnectdk.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내 알림 전체 (최신순)
    List<Notification> findByUserIdOrderByIdDesc(Long userId);

    // 안 읽은 알림 개수 (벨 배지용)
    long countByUserIdAndIsReadFalse(Long userId);
}