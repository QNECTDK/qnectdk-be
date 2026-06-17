package com.qnectdk.domain.group.repository;

import com.qnectdk.domain.group.entity.FriendGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMember, Long> {

    // 특정 그룹의 멤버 전체
    List<FriendGroupMember> findByGroupId(Long groupId);

    // 같은 그룹에 같은 친구 중복 추가 방지
    boolean existsByGroupIdAndFriendId(Long groupId, Long friendId);

    // 그룹에서 특정 멤버 제거용 조회
    java.util.Optional<FriendGroupMember> findByGroupIdAndFriendId(Long groupId, Long friendId);

    // 그룹 삭제 시 멤버 일괄 삭제
    void deleteByGroupId(Long groupId);
}