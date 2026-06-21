package com.qnectdk.domain.friend.controller;

import com.qnectdk.domain.friend.dto.FriendCardResponse;
import com.qnectdk.domain.friend.dto.FriendMemoRequest;
import com.qnectdk.domain.friend.dto.FriendMemoResponse;
import com.qnectdk.domain.friend.dto.FriendRequestDto;
import com.qnectdk.domain.friend.dto.FriendResponse;
import com.qnectdk.domain.friend.dto.FriendSummary;
import com.qnectdk.domain.friend.service.FriendMemoService;
import com.qnectdk.domain.friend.service.FriendService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "친구", description = """
    친구 관계 관리(방향성 "명함첩" 모델).
    [흐름] 상대 QR/URL로 프로필 접속 → '수락' 시 친구 추가(POST /api/friends) → 상호 등록 + 첫 만남 퀴즈 알림 발송.
    '거절'은 클라이언트 동작(팝업 닫기)일 뿐 서버 호출이 없다(보류 상태 없음).
    삭제(DELETE /api/friends/{friendId})는 내 쪽 관계만 끊으며 상대 목록엔 영향이 없다.
    친구 목록은 person 카드(학교·MBTI·관심사 등)와 함께 내려준다. 자동완성(/summaries)은 id+이름+characterId만 제공.
        """)
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendMemoService friendMemoService;

    @Operation(summary = "친구 추가(수락)", description = "QR/URL로 접속한 상대 프로필에서 '수락' 시 호출. 상호 친구로 등록하고 첫 만남 퀴즈 알림을 발송한다. 자기 자신은 추가 불가.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본인을 추가하는 등 입력값이 올바르지 않음 (INVALID_INPUT)")
    })
    @PostMapping
    public ApiResponse<FriendResponse> add(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody FriendRequestDto dto
    ) {
      return ApiResponse.ok(friendService.addFriend(user.getUserId(), dto.friendId()));
    }

    @Operation(summary = "친구 삭제", description = "내 친구 목록에서 해당 친구를 제거한다(내 쪽만). 상대 목록엔 영향 없음.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "내 친구 목록에 없음 (FRIENDSHIP_NOT_FOUND)")
    })
    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "삭제할 친구의 userId", example = "2") @PathVariable Long friendId
    ) {
      friendService.delete(user.getUserId(), friendId);
      return ApiResponse.ok();
    }

    @Operation(summary = "내 친구 목록", description = "내가 저장한 친구 전체를 person 카드와 함께 반환한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @GetMapping
    public ApiResponse<List<FriendCardResponse>> getFriends(
            @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "정렬 기준: recent(저장 최신순, 기본값) 또는 name(이름 가나다순)", example = "recent")
            @RequestParam(defaultValue = "recent") String sort
    ) {
        return ApiResponse.ok(friendService.getFriendCards(user.getUserId(), sort));
    }

    @Operation(summary = "자동완성용 친구 목록", description = "그룹 멤버 추가 시 사용. 친구의 id+이름+characterId만 반환.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @GetMapping("/summaries")
    public ApiResponse<List<FriendSummary>> getFriendSummaries(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getFriendSummaries(user.getUserId()));
    }

    @Operation(summary = "친구 메모 작성/수정", description = "친구에 대한 비공개 메모를 작성하거나 수정한다(upsert). 최대 200자.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @PutMapping("/memos")
    public ApiResponse<FriendMemoResponse> upsertMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody FriendMemoRequest dto
    ) {
        return ApiResponse.ok(friendMemoService.upsert(user.getUserId(), dto.friendId(), dto.content()));
    }

    @Operation(summary = "친구 메모 조회", description = "특정 친구에 대한 내 비공개 메모를 조회한다. 아직 메모가 없으면 200 + 빈 메모(content=null).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공(메모 없으면 content=null)")
    })
    @GetMapping("/memos/{friendId}")
    public ApiResponse<FriendMemoResponse> getMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "메모 대상 친구의 userId", example = "2") @PathVariable Long friendId
    ) {
        return ApiResponse.ok(friendMemoService.get(user.getUserId(), friendId));
    }
}
