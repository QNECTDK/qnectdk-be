package com.qnectdk.domain.quiz.controller;

import com.qnectdk.domain.quiz.dto.AiQuizDraftResponse;
import com.qnectdk.domain.quiz.dto.QuizAttemptRequest;
import com.qnectdk.domain.quiz.dto.QuizResponse;
import com.qnectdk.domain.quiz.dto.QuizResultResponse;
import com.qnectdk.domain.quiz.dto.QuizSaveRequest;
import com.qnectdk.domain.quiz.dto.SolvableQuizResponse;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.service.QuizService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "퀴즈", description = "AI 퀴즈 생성·편집·응시 API")
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "내 퀴즈 조회",
            description = "현재 활성화된 내 퀴즈를 정답 포함 편집 뷰로 반환한다. 아직 없으면 404(QUIZ_NOT_FOUND).")
    @GetMapping("/me")
    public ApiResponse<QuizResponse> getMine(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(quizService.getActiveQuiz(user.getUserId()));
    }

    @Operation(summary = "AI 문제 초안 생성",
            description = "내 프로필 기반으로 AI가 문제 초안 여러 개를 생성해 반환한다(저장하지 않음)."
                    + " 클라이언트가 '내 퀴즈 목록'에 추가·편집한 뒤 PUT /me 로 저장한다. 기존 퀴즈를 덮어쓰지 않는다.")
    @PostMapping("/me/generate")
    public ApiResponse<AiQuizDraftResponse> generate(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "퀴즈 종류(기본 FIRST_MEET)")
            @RequestParam(defaultValue = "FIRST_MEET") QuizType type,
            @Parameter(description = "생성할 문제 수(1~5, 기본 3)")
            @RequestParam(defaultValue = "3") int count) {
        return ApiResponse.ok(quizService.generateDraft(user.getUserId(), type, count));
    }

    @Operation(summary = "퀴즈 편집·저장",
            description = "문항/보기를 직접 작성·수정·추가해 내 활성 퀴즈를 전체 교체한다(문항 3~5개).")
    @PutMapping("/me")
    public ApiResponse<QuizResponse> save(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody QuizSaveRequest request) {
        return ApiResponse.ok(quizService.saveMyQuiz(user.getUserId(), request));
    }

    @Operation(summary = "내 퀴즈 삭제",
            description = "내 활성 퀴즈의 문항·보기를 제거하고 비활성화한다. 활성 퀴즈가 없으면 404(QUIZ_NOT_FOUND).")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMine(@AuthenticationPrincipal CustomUserDetails user) {
        quizService.deleteMyQuiz(user.getUserId());
        return ApiResponse.ok();
    }

    @Operation(summary = "친구 퀴즈 조회",
            description = "친구(ownerId)의 활성 퀴즈를 푸는 화면용으로 조회한다(정답 미노출). 내 프로필 미완성 시 solvable=false.")
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<SolvableQuizResponse> getFriendQuiz(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "퀴즈 주인 사용자 ID") @PathVariable Long ownerId) {
        return ApiResponse.ok(quizService.getSolvableQuiz(ownerId, user.getUserId()));
    }

    @Operation(summary = "퀴즈 응시·채점",
            description = "친구 퀴즈에 답을 제출하고 채점 결과를 받는다. 프로필 완성 필요. 첫 풀기면 포인트가 적립된다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/owner/{ownerId}/attempts")
    public ApiResponse<QuizResultResponse> solve(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "퀴즈 주인 사용자 ID") @PathVariable Long ownerId,
            @Valid @RequestBody QuizAttemptRequest request) {
        return ApiResponse.ok(quizService.solve(ownerId, user.getUserId(), request));
    }

    @Operation(summary = "응시 결과 조회",
            description = "내 응시 기록의 문항별 정/오답을 조회한다.")
    @GetMapping("/attempts/{attemptId}")
    public ApiResponse<QuizResultResponse> getResult(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "응시 ID") @PathVariable Long attemptId) {
        return ApiResponse.ok(quizService.getResult(attemptId, user.getUserId()));
    }
}
