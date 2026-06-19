package com.qnectdk.domain.friend.controller;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "친구", description = """
        친구 관계 관리.
        [흐름] 친구요청(POST /api/friends) → 받은요청 조회(GET /requests/received) → 수락(PATCH /{id}/accept) 또는 거절.
        수락 시 자동: 요청자에게 알림 발송 + 양쪽 30일 리마인드 예약 + 친구수 마일스톤 포인트 체크.
        친구 프로필 상세(학교·MBTI 등)는 이 API에 없음 → A의 프로필 API와 조합. 자동완성(/summaries)은 id+이름만 제공.
        """)
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendMemoService friendMemoService;

    @Operation(summary = "친구 추가 요청", description = "상대(addresseeId)에게 친구 요청을 보낸다. 중복 요청·자기 자신 요청은 거부.")
    @PostMapping
    public ApiResponse<FriendResponse> request(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody FriendRequestDto dto
    ) {
        return ApiResponse.ok(friendService.request(user.getUserId(), dto.addresseeId()));
    }

    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락한다. 요청 받은 사람만 가능. 수락 시 알림·리마인드·마일스톤 자동 처리.")
    @PatchMapping("/{friendshipId}/accept")
    public ApiResponse<FriendResponse> accept(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long friendshipId
    ) {
        return ApiResponse.ok(friendService.accept(friendshipId, user.getUserId()));
    }

    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절한다. 요청 받은 사람만 가능.")
    @PatchMapping("/{friendshipId}/reject")
    public ApiResponse<FriendResponse> reject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long friendshipId
    ) {
        return ApiResponse.ok(friendService.reject(friendshipId, user.getUserId()));
    }

    @Operation(summary = "내 친구 목록", description = "수락된(ACCEPTED) 친구 전체를 반환한다.")
    @GetMapping
    public ApiResponse<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getFriends(user.getUserId()));
    }

    @Operation(summary = "자동완성용 친구 목록", description = "그룹 멤버 추가 시 사용. 친구의 id+이름만 반환.")
    @GetMapping("/summaries")
    public ApiResponse<List<FriendSummary>> getFriendSummaries(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getFriendSummaries(user.getUserId()));
    }

    @Operation(summary = "받은 친구 요청 목록", description = "내가 받은 대기중(PENDING) 친구 요청들을 반환한다.")
    @GetMapping("/requests/received")
    public ApiResponse<List<FriendResponse>> getReceivedRequests(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getReceivedRequests(user.getUserId()));
    }

    @Operation(summary = "친구 메모 작성/수정", description = "친구에 대한 비공개 메모를 작성하거나 수정한다(upsert). 최대 200자.")
    @PutMapping("/memos")
    public ApiResponse<FriendMemoResponse> upsertMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody FriendMemoRequest dto
    ) {
        return ApiResponse.ok(friendMemoService.upsert(user.getUserId(), dto.friendId(), dto.content()));
    }

    @Operation(summary = "친구 메모 조회", description = "특정 친구에 대한 내 비공개 메모를 조회한다.")
    @GetMapping("/memos/{friendId}")
    public ApiResponse<FriendMemoResponse> getMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long friendId
    ) {
        return ApiResponse.ok(friendMemoService.get(user.getUserId(), friendId));
    }
}