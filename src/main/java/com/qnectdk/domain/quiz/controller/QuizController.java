package com.qnectdk.domain.quiz.controller;

import com.qnectdk.domain.quiz.dto.AiQuizDraftResponse;
import com.qnectdk.domain.quiz.dto.FriendQuizResponse;
import com.qnectdk.domain.quiz.dto.QuizAttemptRequest;
import com.qnectdk.domain.quiz.dto.QuizResponse;
import com.qnectdk.domain.quiz.dto.QuizResultResponse;
import com.qnectdk.domain.quiz.dto.QuizSaveRequest;
import com.qnectdk.domain.quiz.dto.ReminderCardResponse;
import com.qnectdk.domain.quiz.dto.SolvableQuizResponse;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.service.FriendQuizService;
import com.qnectdk.domain.quiz.service.QuizService;
import com.qnectdk.domain.quiz.service.ReminderCardService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
    private final FriendQuizService friendQuizService;
    private final ReminderCardService reminderCardService;

    @Operation(summary = "홈 리마인드 카드 조회", description = "홈 '이 사람을 기억하나요?' 카드용 — 오늘 복습 대상 친구 1명과 그 친구의 활성 퀴즈를 반환한다."
        + " 대상이 없으면 data=null.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공(대상 없으면 data=null)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)")
    })
    @GetMapping("/reminders/today")
    public ApiResponse<ReminderCardResponse> getTodayReminderCard(@AuthenticationPrincipal CustomUserDetails user) {
      return ApiResponse.ok(reminderCardService.getTodayCard(user.getUserId()));
    }

    @Operation(summary = "친구 퀴즈 목록 조회", description = "내 친구별 퀴즈 카드 목록을 반환한다. 친구마다 활성 퀴즈 유무(hasQuiz)·총문항 수·내 최근 응시 여부와 정답 수를 담는다."
        + " 활성 퀴즈가 없는 친구도 hasQuiz=false 로 포함된다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"))
    @GetMapping("/friends")
    public ApiResponse<List<FriendQuizResponse>> getFriendQuizzes(@AuthenticationPrincipal CustomUserDetails user) {
      return ApiResponse.ok(friendQuizService.getFriendQuizzes(user.getUserId()));
    }

    @Operation(summary = "내 퀴즈 조회",
            description = "현재 활성화된 내 퀴즈를 정답 포함 편집 뷰로 반환한다. 아직 없으면 404(QUIZ_NOT_FOUND).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "활성화된 퀴즈가 없음 (QUIZ_NOT_FOUND)")
    })
    @GetMapping("/me")
    public ApiResponse<QuizResponse> getMine(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(quizService.getActiveQuiz(user.getUserId()));
    }

    @Operation(summary = "AI 문제 초안 생성",
            description = "내 프로필 기반으로 AI가 문제 초안 여러 개를 생성해 반환한다(저장하지 않음)."
                    + " 클라이언트가 '내 퀴즈 목록'에 추가·편집한 뒤 PUT /me 로 저장한다. 기존 퀴즈를 덮어쓰지 않는다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AI가 생성한 문제 내용이 정합성 검증에 실패함 (QUIZ_INVALID_CONTENT)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "AI 퀴즈 생성 실패 (QUIZ_GENERATION_FAILED)")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "문항/보기 정합성 검증 실패 (QUIZ_INVALID_CONTENT)")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 퀴즈를 조회하려 함 (QUIZ_FORBIDDEN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자의 활성 퀴즈가 없음 (QUIZ_NOT_FOUND)")
    })
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<SolvableQuizResponse> getFriendQuiz(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "퀴즈 주인 사용자 ID") @PathVariable Long ownerId) {
        return ApiResponse.ok(quizService.getSolvableQuiz(ownerId, user.getUserId()));
    }

    @Operation(summary = "퀴즈 응시·채점",
        description = "친구(ownerId) 퀴즈에 답을 제출하고 채점 결과를 받는다. 응시자 프로필이 완성돼야 풀 수 있다."
            + " 해당 친구의 퀴즈를 처음 푼 경우에만 포인트 +10P(QUIZ_FIRST_SOLVE)가 적립된다(친구 1명당 1회, 재응시·재적립 없음)."
            + " 채점 결과는 응시 즉시 반환되며 포인트 적립 성공 여부와 무관하게 201 로 응답한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "응시·채점 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 퀴즈를 응시하려 하거나(QUIZ_FORBIDDEN) 프로필 미완성으로 응시 불가(QUIZ_NOT_SOLVABLE)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자의 활성 퀴즈가 없음 (QUIZ_NOT_FOUND)")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "응시 기록을 찾을 수 없음 (QUIZ_ATTEMPT_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 응시 기록이 아님 (QUIZ_FORBIDDEN)")
    })
    @GetMapping("/attempts/{attemptId}")
    public ApiResponse<QuizResultResponse> getResult(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "응시 ID") @PathVariable Long attemptId) {
        return ApiResponse.ok(quizService.getResult(attemptId, user.getUserId()));
      }
}
