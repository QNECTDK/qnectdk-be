package com.qnectdk.domain.group.service;

import com.qnectdk.domain.friend.entity.FriendshipStatus;
import com.qnectdk.domain.friend.repository.FriendshipRepository;
import com.qnectdk.domain.group.dto.GroupMemberResponse;
import com.qnectdk.domain.group.dto.GroupResponse;
import com.qnectdk.domain.group.entity.FriendGroup;
import com.qnectdk.domain.group.entity.FriendGroupMember;
import com.qnectdk.domain.group.repository.FriendGroupMemberRepository;
import com.qnectdk.domain.group.repository.FriendGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final FriendGroupRepository groupRepository;
    private final FriendGroupMemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository; // 같은 B 도메인, 참조 OK

    // ===== 그룹 =====

    @Transactional
    public GroupResponse createGroup(Long userId, String name, String hashtags) {
        if (groupRepository.existsByUserIdAndName(userId, name)) {
            throw new IllegalArgumentException("이미 같은 이름의 그룹이 있습니다.");
        }
        FriendGroup saved = groupRepository.save(
                FriendGroup.create(userId, name, hashtags)
        );
        return GroupResponse.from(saved);
    }

    @Transactional
    public GroupResponse updateGroup(Long userId, Long groupId, String name, String hashtags) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        // 이름을 바꾸는 경우, 다른 그룹과 중복되는지 체크
        if (!group.getName().equals(name)
                && groupRepository.existsByUserIdAndName(userId, name)) {
            throw new IllegalArgumentException("이미 같은 이름의 그룹이 있습니다.");
        }
        group.update(name, hashtags);
        return GroupResponse.from(group);
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        memberRepository.deleteByGroupId(group.getId()); // 멤버 먼저 정리
        groupRepository.delete(group);
    }

    public List<GroupResponse> getMyGroups(Long userId) {
        return groupRepository.findByUserId(userId).stream()
                .map(GroupResponse::from)
                .toList();
    }

    public List<GroupResponse> searchByName(Long userId, String keyword) {
        return groupRepository
                .findByUserIdAndNameContainingIgnoreCase(userId, keyword).stream()
                .map(GroupResponse::from)
                .toList();
    }

    // ===== 멤버 =====

    @Transactional
    public GroupMemberResponse addMember(Long userId, Long groupId, Long friendId) {
        FriendGroup group = getOwnedGroup(userId, groupId);

        // 핵심 규칙: 추가하려는 대상이 내 ACCEPTED 친구여야 함
        boolean isFriend = friendshipRepository
                .existsAcceptedBetween(userId, friendId, FriendshipStatus.ACCEPTED);
        if (!isFriend) {
            throw new IllegalArgumentException("수락된 친구만 그룹에 추가할 수 있습니다.");
        }

        if (memberRepository.existsByGroupIdAndFriendId(group.getId(), friendId)) {
            throw new IllegalArgumentException("이미 그룹에 추가된 친구입니다.");
        }

        FriendGroupMember saved = memberRepository.save(
                FriendGroupMember.of(group.getId(), friendId)
        );
        return GroupMemberResponse.from(saved);
    }

    @Transactional
    public void removeMember(Long userId, Long groupId, Long friendId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        FriendGroupMember member = memberRepository
                .findByGroupIdAndFriendId(group.getId(), friendId)
                .orElseThrow(() -> new IllegalArgumentException("그룹에 없는 멤버입니다."));
        memberRepository.delete(member);
    }

    public List<GroupMemberResponse> getMembers(Long userId, Long groupId) {
        FriendGroup group = getOwnedGroup(userId, groupId);
        return memberRepository.findByGroupId(group.getId()).stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    // ===== private =====

    // 그룹이 존재하고, 그 그룹의 주인이 나인지 확인
    private FriendGroup getOwnedGroup(Long userId, Long groupId) {
        FriendGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다. id=" + groupId));
        if (!group.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 그룹만 관리할 수 있습니다.");
        }
        return group;
    }
}