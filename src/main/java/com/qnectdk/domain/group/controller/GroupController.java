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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "그룹", description = """
        친구를 묶는 그룹(카테고리). 무료 5개, 초과 시 생성마다 10P 차감.
        [생성 진입 2가지] (1) 그룹 생성 화면: 친구 이름 '검색'으로 멤버 등록  (2) 친구 목록 화면: 친구 여러 명 '선택' 후 그룹 생성.
        두 경로 모두 friendIds(친구 id 배열)를 담아 POST /api/groups/with-members 한 번 호출(권장).
        멤버는 내 ACCEPTED 친구만 가능. 그룹명은 사용자별 중복 불가.
        인원수는 멤버 목록 길이로 카운트(저장값 없음). 해시태그는 통짜 텍스트(프론트가 분리 렌더).
        """)
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "멤버 없이 그룹만 생성한다. 5개까지 무료, 6번째부터는 생성 시 포인트 10점 차감.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "같은 이름의 그룹이 이미 있음 (DUPLICATE_GROUP_NAME)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "무료 개수 초과인데 포인트가 부족함 (INSUFFICIENT_POINT)")
    })
    @PostMapping
    public ApiResponse<GroupResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody GroupCreateRequest dto
    ) {
        return ApiResponse.ok(groupService.createGroup(user.getUserId(), dto.name(), dto.hashtags()));
    }

    @Operation(summary = "그룹 생성 및 멤버 추가", description = "그룹 이름, 해시태그, 친구 ID 목록을 받아 그룹을 생성하고 선택한 친구들을 멤버로 추가합니다. 5개까지 무료, 6번째부터는 생성 시 포인트 10점 차감.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "같은 이름의 그룹이 이미 있음 (DUPLICATE_GROUP_NAME)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ACCEPTED 상태가 아닌 친구를 멤버로 추가함 (NOT_ACCEPTED_FRIEND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 그룹에 추가된 친구이거나, 무료 개수 초과인데 포인트가 부족함 (ALREADY_GROUP_MEMBER / INSUFFICIENT_POINT)")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 그룹이 아님 (NOT_GROUP_OWNER)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "같은 이름의 그룹이 이미 있음 (DUPLICATE_GROUP_NAME)")
    })
    @PutMapping("/{groupId}")
    public ApiResponse<GroupResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "그룹 id", example = "1") @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest dto
    ) {
        return ApiResponse.ok(groupService.updateGroup(user.getUserId(), groupId, dto.name(), dto.hashtags()));
    }

    @Operation(summary = "그룹 삭제", description = "그룹을 삭제한다. 멤버도 함께 삭제됨. 본인 그룹만 가능.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 그룹이 아님 (NOT_GROUP_OWNER)")
    })
    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "그룹 id", example = "1") @PathVariable Long groupId
    ) {
        groupService.deleteGroup(user.getUserId(), groupId);
        return ApiResponse.ok();
    }

    @Operation(summary = "내 그룹 목록", description = "로그인한 사용자가 생성한 그룹 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @GetMapping
    public ApiResponse<List<GroupResponse>> myGroups(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(groupService.getMyGroups(user.getUserId()));
    }

    @Operation(summary = "그룹명 검색", description = "그룹 이름으로 검색한다(해시태그는 검색 안 함).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    @GetMapping("/search")
    public ApiResponse<List<GroupResponse>> search(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "그룹명 검색 키워드", example = "동기") @RequestParam String keyword
    ) {
        return ApiResponse.ok(groupService.searchByName(user.getUserId(), keyword));
    }

    @Operation(summary = "그룹에 멤버 추가", description = "그룹에 친구를 추가한다. 추가한 친구만 가능.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 그룹이 아님 (NOT_GROUP_OWNER)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ACCEPTED 상태가 아닌 친구임 (NOT_ACCEPTED_FRIEND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 그룹에 추가된 친구임 (ALREADY_GROUP_MEMBER)")
    })
    @PostMapping("/{groupId}/members")
    public ApiResponse<GroupMemberResponse> addMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "그룹 id", example = "1") @PathVariable Long groupId,
            @Valid @RequestBody GroupMemberAddRequest dto
    ) {
        return ApiResponse.ok(groupService.addMember(user.getUserId(), groupId, dto.friendId()));
    }

    @Operation(summary = "그룹 멤버 목록 조회", description = "지정한 그룹에 속한 멤버 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 그룹이 아님 (NOT_GROUP_OWNER)")
    })
    @GetMapping("/{groupId}/members")
    public ApiResponse<List<GroupMemberResponse>> members(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "그룹 id", example = "1") @PathVariable Long groupId
    ) {
        return ApiResponse.ok(groupService.getMembers(user.getUserId(), groupId));
    }

    @Operation(summary = "그룹에서 멤버 제거", description = "그룹에서 특정 멤버를 제거한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹에 없는 멤버임 (GROUP_MEMBER_NOT_FOUND)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 그룹이 아님 (NOT_GROUP_OWNER)")
    })
    @DeleteMapping("/{groupId}/members/{friendId}")
    public ApiResponse<Void> removeMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "그룹 id", example = "1") @PathVariable Long groupId,
            @Parameter(description = "제거할 멤버(친구)의 userId", example = "2") @PathVariable Long friendId
    ) {
        groupService.removeMember(user.getUserId(), groupId, friendId);
        return ApiResponse.ok();
    }
}