package com.qnectdk.domain.point.entity;

public final class PointPolicy {

    private PointPolicy() {}

    public static final int ATTENDANCE = 5;            // 출석 5P
    public static final int DAILY_ANSWER = 5; // 데일리 밸런스 답변 5P (하루 1회)
    public static final int QUIZ_FIRST_SOLVE = 10;     // 친구 첫 퀴즈 풀기 10P
    public static final int PROFILE_CREATE = 20;       // 프로필 최초 생성 20P
    public static final int QUIZ_FIRST_SETUP = 20;     // 퀴즈 최초 설정 20P

    // 친구 수 달성 (단계별, 5/15/30명에서 1회씩)
    public static final int FRIEND_MILESTONE_5 = 10;
    public static final int FRIEND_MILESTONE_15 = 20;
    public static final int FRIEND_MILESTONE_30 = 50;
}