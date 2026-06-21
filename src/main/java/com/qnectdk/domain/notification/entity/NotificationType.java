package com.qnectdk.domain.notification.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 종류. 프론트는 type+refId로 이동 화면을 결정한다.")
public enum NotificationType {
  @Schema(description = "새 친구 요청 도착. refId=요청 보낸 사람의 userId → 받은 요청/수락 화면으로 이동")
  FRIEND_REQUEST,
        @Schema(description = "새 친구 추가됨. refId=친구(상대)의 userId → 친구 프로필로 이동") FRIEND_ADD,
    @Schema(description = "오늘의 데일리 퀴즈 생성됨. refId=데일리 퀴즈 id → 데일리 화면으로 이동") DAILY_QUIZ,
    @Schema(description = "30일 리마인드 '기억나세요?'. refId=리마인드 퀴즈 id → 해당 퀴즈로 이동") QUIZ_REMIND
}