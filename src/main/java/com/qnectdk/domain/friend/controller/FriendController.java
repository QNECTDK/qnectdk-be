package com.qnectdk.domain.friend.controller;

import com.qnectdk.domain.friend.dto.FriendMemoRequest;
import com.qnectdk.domain.friend.dto.FriendMemoResponse;
import com.qnectdk.domain.friend.dto.FriendRequestDto;
import com.qnectdk.domain.friend.dto.FriendResponse;
import com.qnectdk.domain.friend.service.FriendMemoService;
import com.qnectdk.domain.friend.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendMemoService friendMemoService;

    // TODO: JWT 붙으면 currentUserId를 @AuthenticationPrincipal에서 꺼내도록 교체.
    //       지금은 임시로 헤더 X-USER-ID로 "내 ID"를 받는다.

    // 친구 추가 요청
    @PostMapping
    public FriendResponse request(
            @RequestHeader("X-USER-ID") Long currentUserId,
            @Valid @RequestBody FriendRequestDto dto
    ) {
        return friendService.request(currentUserId, dto.addresseeId());
    }

    // 친구 요청 수락
    @PatchMapping("/{friendshipId}/accept")
    public FriendResponse accept(
            @RequestHeader("X-USER-ID") Long currentUserId,
            @PathVariable Long friendshipId
    ) {
        return friendService.accept(friendshipId, currentUserId);
    }

    // 친구 요청 거절
    @PatchMapping("/{friendshipId}/reject")
    public FriendResponse reject(
            @RequestHeader("X-USER-ID") Long currentUserId,
            @PathVariable Long friendshipId
    ) {
        return friendService.reject(friendshipId, currentUserId);
    }

    // 내 친구 목록
    @GetMapping
    public List<FriendResponse> getFriends(
            @RequestHeader("X-USER-ID") Long currentUserId
    ) {
        return friendService.getFriends(currentUserId);
    }

    // 내가 받은 친구 요청 목록
    @GetMapping("/requests/received")
    public List<FriendResponse> getReceivedRequests(
            @RequestHeader("X-USER-ID") Long currentUserId
    ) {
        return friendService.getReceivedRequests(currentUserId);
    }

    // 친구 메모 작성/수정 (있으면 수정, 없으면 생성)
    @PutMapping("/memos")
    public FriendMemoResponse upsertMemo(
            @RequestHeader("X-USER-ID") Long currentUserId,
            @Valid @RequestBody FriendMemoRequest dto
    ) {
        return friendMemoService.upsert(currentUserId, dto.friendId(), dto.content());
    }

    // 특정 친구에 대한 내 메모 조회
    @GetMapping("/memos/{friendId}")
    public FriendMemoResponse getMemo(
            @RequestHeader("X-USER-ID") Long currentUserId,
            @PathVariable Long friendId
    ) {
        return friendMemoService.get(currentUserId, friendId);
    }
}