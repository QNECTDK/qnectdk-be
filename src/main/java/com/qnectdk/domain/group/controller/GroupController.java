package com.qnectdk.domain.group.controller;

import com.qnectdk.domain.group.dto.GroupCreateRequest;
import com.qnectdk.domain.group.dto.GroupCreateWithMembersRequest;
import com.qnectdk.domain.group.dto.GroupMemberAddRequest;
import com.qnectdk.domain.group.dto.GroupMemberResponse;
import com.qnectdk.domain.group.dto.GroupResponse;
import com.qnectdk.domain.group.dto.GroupUpdateRequest;
import com.qnectdk.domain.group.dto.GroupWithMembersResponse;
import com.qnectdk.domain.group.service.GroupService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "그룹", description = "그룹 생성/수정/삭제/검색, 멤버 관리 API")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "멤버 없이 그룹만 생성한다. 무료 3개 제한.")
    @PostMapping
    public ApiResponse<GroupResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody GroupCreateRequest dto
    ) {
        return ApiResponse.ok(groupService.createGroup(user.getUserId(), dto.name(), dto.hashtags()));
    }

    @Operation(summary = "그룹 생성 및 멤버 추가", description = "그룹 이름, 해시태그, 친구 ID 목록을 받아 그룹을 생성하고 선택한 친구들을 멤버로 추가합니다.")
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

    @Operation(summary = "그룹 수정", description = "그룹 이름·해시태그를 수정한다. 본인 그룹만 가능.")
    @PutMapping("/{groupId}")
    public ApiResponse<GroupResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest dto
    ) {
        return ApiResponse.ok(groupService.updateGroup(user.getUserId(), groupId, dto.name(), dto.hashtags()));
    }

    @Operation(summary = "그룹 삭제", description = "그룹을 삭제한다. 멤버도 함께 삭제됨. 본인 그룹만 가능.")
    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId
    ) {
        groupService.deleteGroup(user.getUserId(), groupId);
        return ApiResponse.ok();
    }

    @Operation(summary = "내 그룹 목록", description = "로그인한 사용자가 생성한 그룹 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<GroupResponse>> myGroups(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(groupService.getMyGroups(user.getUserId()));
    }

    @Operation(summary = "그룹명 검색", description = "그룹 이름으로 검색한다(해시태그는 검색 안 함).")
    @GetMapping("/search")
    public ApiResponse<List<GroupResponse>> search(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String keyword
    ) {
        return ApiResponse.ok(groupService.searchByName(user.getUserId(), keyword));
    }

    @Operation(summary = "그룹에 멤버 추가", description = "그룹에 친구를 추가한다. 추가한 친구만 가능.")
    @PostMapping("/{groupId}/members")
    public ApiResponse<GroupMemberResponse> addMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMemberAddRequest dto
    ) {
        return ApiResponse.ok(groupService.addMember(user.getUserId(), groupId, dto.friendId()));
    }

    @Operation(summary = "그룹 멤버 목록 조회", description = "지정한 그룹에 속한 멤버 목록을 조회합니다.")
    @GetMapping("/{groupId}/members")
    public ApiResponse<List<GroupMemberResponse>> members(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId
    ) {
        return ApiResponse.ok(groupService.getMembers(user.getUserId(), groupId));
    }

    @Operation(summary = "그룹에서 멤버 제거", description = "그룹에서 특정 멤버를 제거한다.")
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