package com.qnectdk.domain.point.entity;

public enum PointReason {
    ATTENDANCE,          // 출석 5P
    PROFILE_CREATE,      // 프로필 최초 생성
    QUIZ_FIRST_SETUP,    // 퀴즈 최초 설정
    QUIZ_FIRST_SOLVE,    // 친구 첫 퀴즈 풀기 10P
    FRIEND_MILESTONE,    // 친구 N명 달성
    SHOP_PURCHASE        // 상점 구매(차감)
}