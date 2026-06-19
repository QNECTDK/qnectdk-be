package com.qnectdk.domain.notification.controller;

import com.qnectdk.domain.notification.dto.NotificationResponse;
import com.qnectdk.domain.notification.service.NotificationService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "알림", description = """
        인앱 알림(벨). 백엔드가 이벤트 발생 시 자동 생성하며, 프론트는 조회/읽음만.
        [흐름] 목록(GET /api/notifications) + 안읽은수(GET /unread-count, 벨 배지) → 클릭 시 읽음(PATCH /{id}/read).
        type+refId로 이동 화면 결정(NotificationType 스키마 참고). 여러 명이 추가해도 한 명당 한 줄씩 쌓임.
        """)
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록", description = "내 알림을 최신순으로 반환한다. type+refId로 화면 이동 결정.")
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(notificationService.getMyNotifications(user.getUserId()));
    }

    @Operation(summary = "안 읽은 알림 개수", description = "읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(user.getUserId()));
    }

    @Operation(summary = "알림 읽음 처리", description = "읽은 알림을 읽음 상태로 변경한다.")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(user.getUserId(), notificationId);
        return ApiResponse.ok();
    }
}