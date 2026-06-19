package com.qnectdk.domain.notification.entity;

public enum NotificationType {
    FRIEND_ADD,    // 새 친구 추가됨 (refId = 친구 userId)
    DAILY_QUIZ,    // 오늘의 데일리 퀴즈 생성됨 (refId = 데일리 퀴즈 id)
    QUIZ_REMIND    // 30일 리마인드 "기억나세요?" (refId = 리마인드 퀴즈 id)
}