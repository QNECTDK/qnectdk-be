package com.qnectdk.domain.group.service;

import com.qnectdk.domain.friend.entity.FriendshipStatus;
import com.qnectdk.domain.friend.repository.FriendshipRepository;
import com.qnectdk.domain.group.dto.GroupMemberResponse;
import com.qnectdk.domain.group.dto.GroupResponse;
import com.qnectdk.domain.group.dto.GroupSummaryResponse;
import com.qnectdk.domain.group.entity.FriendGroup;
import com.qnectdk.domain.group.entity.FriendGroupMember;
import com.qnectdk.domain.group.repository.FriendGroupMemberRepository;
import com.qnectdk.domain.group.repository.FriendGroupRepository;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.qnectdk.domain.group.dto.GroupWithMembersResponse;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private static final int FREE_GROUP_LIMIT = 5; // 무료 5개, 이후부터는 포인트 차감으로 생성 가능
    private static final int GROUP_CREATE_COST = 10; // 무료 개수 초과 시 그룹 생성 차감 포인트

    private final FriendGroupRepository groupRepository;
    private final FriendGroupMemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final PointService pointService;

    @Transactional
    public GroupResponse createGroup(Long userId, String name, String hashtags) {
        boolean overFreeLimit = groupRepository.countByUserId(userId) >= FREE_GROUP_LIMIT;
        if (groupRepository.existsByUserIdAndName(userId, name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GROUP_NAME);
        }
        FriendGroup saved = groupRepository.save(FriendGroup.create(userId, name, hashtags));
        if (overFreeLimit) {
            pointService.spend(userId, GROUP_CREATE_COST, PointReason.GROUP_CREATE, saved.getId());
        }
        return GroupResponse.from(saved);
    }

    // 그룹 생성 + 멤버 여러 명을 한 번에 (한 트랜잭션)
    @Transactional
    public GroupWithMembersResponse createGroupWithMembers(
            Long userId, String name, List<String> hashtags, List<Long> friendIds) {

        // 1) 그룹 생성 (무료 개수 초과 시 차감 + 이름 중복 검증 포함)
        boolean overFreeLimit = groupRepository.countByUserId(userId) >= FREE_GROUP_LIMIT;
        if (groupRepository.existsByUserIdAndName(userId, name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GROUP_NAME);
        }
        FriendGroup group = groupRepository.save(FriendGroup.create(userId, name, joinHashtags(hashtags)));
        if (overFreeLimit) {
            pointService.spend(userId, GROUP_CREATE_COST, PointReason.GROUP_CREATE, group.getId());
        }

        // 2) 멤버 추가 (각각 ACCEPTED 친구 검증 + 중복 방지)
        List<GroupMemberResponse> memberResponses = new java.util.ArrayList<>();
        if (friendIds != null) {
            for (Long friendId : friendIds) {
                boolean isFriend = friendshipRepository
                        .existsAcceptedBetween(userId, friendId, FriendshipStatus.ACCEPTED);
                if (!isFriend) {
                    throw new BusinessException(ErrorCode.NOT_ACCEPTED_FRIEND);
                }
                if (memberRepository.existsByGroupIdAndFriendId(group.getId(), friendId)) {
                    throw new BusinessException(ErrorCode.ALREADY_GROUP_MEMBER);
                }
                FriendGroupMember saved =
                        memberRepository.save(FriendGroupMember.of(group.getId(), friendId));
                memberResponses.add(GroupMemberResponse.from(saved));
            }
        }

        return new GroupWithMembersResponse(GroupResponse.from(group), memberResponses);
    }

    @Transactional
    public GroupResponse updateGroup(Long userId, Long groupId, String name, String hashtags) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        if (!group.getName().equals(name)
                && groupRepository.existsByUserIdAndName(userId, name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GROUP_NAME);
        }
        group.update(name, hashtags);
        return GroupResponse.from(group);
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        memberRepository.deleteByGroupId(group.getId());
        groupRepository.delete(group);
    }

    public List<GroupSummaryResponse> getMyGroups(Long userId) {
      return toSummaries(groupRepository.findByUserId(userId));
    }

    /** 본인 소유 그룹 단건 조회 (이름 등 기본 정보용). */
    public GroupResponse getGroup(Long userId, Long groupId) {
      return GroupResponse.from(getOwnedGroup(userId, groupId));
    }

    public List<GroupSummaryResponse> searchByName(Long userId, String keyword) {
      return toSummaries(groupRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword));
    }

    /** 그룹 목록을 멤버 수까지 채운 요약 응답으로 변환한다(멤버 수는 한 번의 집계 쿼리로). */
    private List<GroupSummaryResponse> toSummaries(List<FriendGroup> groups) {
        if (groups.isEmpty()) {
            return List.of();
        }
        List<Long> groupIds = groups.stream().map(FriendGroup::getId).toList();
        Map<Long, Long> countByGroupId = memberRepository.countByGroupIdIn(groupIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        return groups.stream()
                .map(g -> GroupSummaryResponse.from(g, countByGroupId.getOrDefault(g.getId(), 0L).intValue()))
                .toList();
    }

    @Transactional
    public GroupMemberResponse addMember(Long userId, Long groupId, Long friendId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        boolean isFriend = friendshipRepository
                .existsAcceptedBetween(userId, friendId, FriendshipStatus.ACCEPTED);
        if (!isFriend) {
            throw new BusinessException(ErrorCode.NOT_ACCEPTED_FRIEND);
        }
        if (memberRepository.existsByGroupIdAndFriendId(group.getId(), friendId)) {
            throw new BusinessException(ErrorCode.ALREADY_GROUP_MEMBER);
        }
        FriendGroupMember saved = memberRepository.save(FriendGroupMember.of(group.getId(), friendId));
        return GroupMemberResponse.from(saved);
    }

    @Transactional
    public void removeMember(Long userId, Long groupId, Long friendId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        FriendGroupMember member = memberRepository
                .findByGroupIdAndFriendId(group.getId(), friendId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
        memberRepository.delete(member);
    }

    public List<GroupMemberResponse> getMembers(Long userId, Long groupId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        return memberRepository.findByGroupId(group.getId()).stream()
                .map(GroupMemberResponse::from).toList();
    }

    /**
     * viewer의 그룹들 기준, 각 friend가 속한 그룹 이름 목록(groupTags).
     * friendId → [그룹이름들]. 어느 그룹에도 없는 friend는 맵에 없음.
     */
    public Map<Long, List<String>> getGroupNamesByMember(Long viewerId, Collection<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return Map.of();
        }
        return memberRepository.findFriendGroupNames(viewerId, friendIds).stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (String) row[1], Collectors.toList())
                ));
    }

    private static String joinHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return null;
        }
        String joined = hashtags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                .collect(Collectors.joining(" "));
        return joined.isBlank() ? null : joined;
    }

    private FriendGroup getOwnedGroup(Long userId, Long groupId) {
        FriendGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
        if (!group.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_GROUP_OWNER);
        }
        return group;
    }
}