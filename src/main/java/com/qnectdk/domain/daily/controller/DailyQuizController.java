package com.qnectdk.domain.daily.controller;

import com.qnectdk.domain.daily.dto.DailyAnswerRequest;
import com.qnectdk.domain.daily.dto.DailyStatsResponse;
import com.qnectdk.domain.daily.dto.DailyTodayResponse;
import com.qnectdk.domain.daily.service.DailyQuizService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "데일리", description = "데일리 밸런스 게임 + 통계 API")
@RestController
@RequestMapping("/api/daily")
@RequiredArgsConstructor
public class DailyQuizController {

    private final DailyQuizService dailyQuizService;

    @Operation(summary = "오늘의 데일리 조회",
            description = "오늘의 밸런스 질문과 내 답변 상태를 반환한다. 통계는 답한 뒤 별도 조회.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오늘의 데일리 퀴즈가 없음 (DAILY_QUIZ_NOT_FOUND)")
    })
    @GetMapping("/today")
    public ApiResponse<DailyTodayResponse> getToday(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(dailyQuizService.getToday(user.getUserId()));
    }

    @Operation(summary = "데일리 답변 제출",
            description = "오늘의 데일리에 A/B 로 답한다(1인 1회). 제출 후 통계를 함께 반환한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "답변 제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오늘의 데일리 퀴즈가 없음 (DAILY_QUIZ_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "오늘 이미 답변함 (DAILY_ALREADY_ANSWERED)")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/today/answer")
    public ApiResponse<DailyStatsResponse> answer(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody DailyAnswerRequest request) {
        return ApiResponse.ok(dailyQuizService.submitAnswer(user.getUserId(), request));
    }

    @Operation(summary = "데일리 통계 조회",
            description = "전체/친구 A·B 비율과 친구별 개별 선택을 반환한다. 내가 먼저 답해야 공개(미답변 시 403).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오늘의 데일리 퀴즈가 없음 (DAILY_QUIZ_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "아직 답변하지 않아 통계를 볼 수 없음 (DAILY_NOT_ANSWERED_YET)")
    })
    @GetMapping("/today/stats")
    public ApiResponse<DailyStatsResponse> stats(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(dailyQuizService.getStats(user.getUserId()));
    }
}
