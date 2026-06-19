package com.qnectdk.domain.point.controller;

import com.qnectdk.domain.point.dto.PointBalanceResponse;
import com.qnectdk.domain.point.dto.PointTransactionResponse;
import com.qnectdk.domain.point.service.AttendanceService;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "포인트", description = """
        포인트(원장 방식: 거래내역 + 잔액캐시).
        [흐름] 앱 진입 시 출석(POST /api/points/attendance, 하루 1회 5P) → 잔액(GET /balance) → 내역(GET /transactions).
        대부분 적립은 백엔드 자동(친구 수락, 퀴즈 등). 프론트가 직접 호출하는 적립은 출석뿐.
        잔액은 현재 포인트를 즉시 조회하며, 거래 내역은 적립·차감 기록을 최신순으로 반환한다.
        """)
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final AttendanceService attendanceService;

    @Operation(summary = "내 포인트 잔액", description = "현재 포인트 잔액을 반환한다(포인트 값).")
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getBalance(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int balance = pointService.getBalance(user.getUserId());
        return ApiResponse.ok(new PointBalanceResponse(balance));
    }

    @Operation(summary = "포인트 거래 내역", description = "통장 내역처럼 적립/차감 기록을 최신순으로 반환한다.")
    @GetMapping("/transactions")
    public ApiResponse<List<PointTransactionResponse>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(pointService.getMyTransactions(user.getUserId()));
    }

    @Operation(summary = "출석 체크", description = "하루 1회 5P 지급. 이미 출석했으면 earnedToday=false(에러 아님).")
    @PostMapping("/attendance")
    public ApiResponse<AttendanceResult> checkAttendance(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        boolean earned = attendanceService.checkToday(user.getUserId());
        int balance = pointService.getBalance(user.getUserId());
        return ApiResponse.ok(new AttendanceResult(earned, balance));
    }

    public record AttendanceResult(boolean earnedToday, int currentBalance) {}
}