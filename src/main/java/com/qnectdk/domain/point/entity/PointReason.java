package com.qnectdk.domain.point.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 적립/차감 사유. 적립은 +, 차감은 -.")
public enum PointReason {
    @Schema(description = "출석 체크 (+5P, 하루 1회)") ATTENDANCE,
    @Schema(description = "프로필 최초 생성 (+20P, 1회)") PROFILE_CREATE,
    @Schema(description = "퀴즈 최초 설정 (+20P, 1회)") QUIZ_FIRST_SETUP,
    @Schema(description = "친구 첫 퀴즈 풀기 (+10P, 친구당 1회)") QUIZ_FIRST_SOLVE,
    @Schema(description = "친구 수 달성 (5명+10P / 15명+20P / 30명+50P)") FRIEND_MILESTONE,
    @Schema(description = "상점 구매 (차감)") SHOP_PURCHASE,
    @Schema(description = "그룹 생성 (무료 5개 초과 시 -10P 차감)") GROUP_CREATE
}