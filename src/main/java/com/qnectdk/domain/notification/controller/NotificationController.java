package com.qnectdk.domain.notification.controller;

import com.qnectdk.domain.notification.dto.NotificationResponse;
import com.qnectdk.domain.notification.service.NotificationService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(notificationService.getMyNotifications(user.getUserId()));
    }

    // 안 읽은 알림 개수 (벨 배지)
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(user.getUserId()));
    }

    // 알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(user.getUserId(), notificationId);
        return ApiResponse.ok();
    }
}