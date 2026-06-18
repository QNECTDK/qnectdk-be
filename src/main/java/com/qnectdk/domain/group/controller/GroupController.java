package com.qnectdk.domain.group.controller;

import com.qnectdk.domain.group.dto.GroupCreateRequest;
import com.qnectdk.domain.group.dto.GroupMemberAddRequest;
import com.qnectdk.domain.group.dto.GroupMemberResponse;
import com.qnectdk.domain.group.dto.GroupResponse;
import com.qnectdk.domain.group.dto.GroupUpdateRequest;
import com.qnectdk.domain.group.service.GroupService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.qnectdk.domain.group.dto.GroupCreateWithMembersRequest;
import com.qnectdk.domain.group.dto.GroupWithMembersResponse;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // 그룹 생성
    @PostMapping
    public ApiResponse<GroupResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody GroupCreateRequest dto
    ) {
        return ApiResponse.ok(groupService.createGroup(user.getUserId(), dto.name(), dto.hashtags()));
    }

    // 그룹 생성 + 멤버 한 번에 (화면: 이름+구성원+태그 입력 후 "그룹 만들기")
    @PostMapping("/with-members")
    public ApiResponse<GroupWithMembersResponse> createWithMembers(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody GroupCreateWithMembersRequest dto
    ) {
        return ApiResponse.ok(
                groupService.createGroupWithMembers(
                        user.getUserId(), dto.name(), dto.hashtags(), dto.friendIds())
        );
    }

    // 그룹 수정
    @PutMapping("/{groupId}")
    public ApiResponse<GroupResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest dto
    ) {
        return ApiResponse.ok(groupService.updateGroup(user.getUserId(), groupId, dto.name(), dto.hashtags()));
    }

    // 그룹 삭제
    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId
    ) {
        groupService.deleteGroup(user.getUserId(), groupId);
        return ApiResponse.ok();
    }

    // 내 그룹 목록
    @GetMapping
    public ApiResponse<List<GroupResponse>> myGroups(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(groupService.getMyGroups(user.getUserId()));
    }

    // 그룹명 검색 (?keyword=...)
    @GetMapping("/search")
    public ApiResponse<List<GroupResponse>> search(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String keyword
    ) {
        return ApiResponse.ok(groupService.searchByName(user.getUserId(), keyword));
    }

    // 그룹에 멤버 추가
    @PostMapping("/{groupId}/members")
    public ApiResponse<GroupMemberResponse> addMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMemberAddRequest dto
    ) {
        return ApiResponse.ok(groupService.addMember(user.getUserId(), groupId, dto.friendId()));
    }

    // 그룹 멤버 목록
    @GetMapping("/{groupId}/members")
    public ApiResponse<List<GroupMemberResponse>> members(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId
    ) {
        return ApiResponse.ok(groupService.getMembers(user.getUserId(), groupId));
    }

    // 그룹에서 멤버 제거
    @DeleteMapping("/{groupId}/members/{friendId}")
    public ApiResponse<Void> removeMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @PathVariable Long friendId
    ) {
        groupService.removeMember(user.getUserId(), groupId, friendId);
        return ApiResponse.ok();
    }
}