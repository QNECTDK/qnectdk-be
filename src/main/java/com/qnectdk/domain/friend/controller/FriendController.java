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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendMemoService friendMemoService;

    // 친구 추가 요청
    @PostMapping
    public ApiResponse<FriendResponse> request(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody FriendRequestDto dto
    ) {
        return ApiResponse.ok(friendService.request(user.getUserId(), dto.addresseeId()));
    }

    // 친구 요청 수락
    @PatchMapping("/{friendshipId}/accept")
    public ApiResponse<FriendResponse> accept(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long friendshipId
    ) {
        return ApiResponse.ok(friendService.accept(friendshipId, user.getUserId()));
    }

    // 친구 요청 거절
    @PatchMapping("/{friendshipId}/reject")
    public ApiResponse<FriendResponse> reject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long friendshipId
    ) {
        return ApiResponse.ok(friendService.reject(friendshipId, user.getUserId()));
    }

    // 내 친구 목록
    @GetMapping
    public ApiResponse<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getFriends(user.getUserId()));
    }

    // 자동완성용 친구 목록 (id + 이름)
    @GetMapping("/summaries")
    public ApiResponse<List<FriendSummary>> getFriendSummaries(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getFriendSummaries(user.getUserId()));
    }

    // 내가 받은 친구 요청 목록
    @GetMapping("/requests/received")
    public ApiResponse<List<FriendResponse>> getReceivedRequests(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(friendService.getReceivedRequests(user.getUserId()));
    }

    // 친구 메모 작성/수정
    @PutMapping("/memos")
    public ApiResponse<FriendMemoResponse> upsertMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody FriendMemoRequest dto
    ) {
        return ApiResponse.ok(friendMemoService.upsert(user.getUserId(), dto.friendId(), dto.content()));
    }

    // 특정 친구에 대한 내 메모 조회
    @GetMapping("/memos/{friendId}")
    public ApiResponse<FriendMemoResponse> getMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long friendId
    ) {
        return ApiResponse.ok(friendMemoService.get(user.getUserId(), friendId));
    }
}