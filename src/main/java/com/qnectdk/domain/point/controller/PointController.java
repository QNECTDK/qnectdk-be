package com.qnectdk.domain.point.controller;

import com.qnectdk.domain.point.dto.PointBalanceResponse;
import com.qnectdk.domain.point.dto.PointTransactionResponse;
import com.qnectdk.domain.point.service.AttendanceService;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final AttendanceService attendanceService;

    // 내 현재 포인트 잔액
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getBalance(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int balance = pointService.getBalance(user.getUserId());
        return ApiResponse.ok(new PointBalanceResponse(balance));
    }

    // 내 포인트 거래 내역 (최신순)
    @GetMapping("/transactions")
    public ApiResponse<List<PointTransactionResponse>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(pointService.getMyTransactions(user.getUserId()));
    }

    // 출석 체크 (하루 1회 5P)
    @PostMapping("/attendance")
    public ApiResponse<AttendanceResult> checkAttendance(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        boolean earned = attendanceService.checkToday(user.getUserId());
        int balance = pointService.getBalance(user.getUserId());
        return ApiResponse.ok(new AttendanceResult(earned, balance));
    }

    // 출석 결과 응답 (이 컨트롤러 안에서만 쓰는 간단한 record)
    public record AttendanceResult(boolean earnedToday, int currentBalance) {}
}