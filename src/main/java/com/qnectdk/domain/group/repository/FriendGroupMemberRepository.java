package com.qnectdk.domain.group.repository;

import com.qnectdk.domain.group.entity.FriendGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMember, Long> {

    /**
     * viewer(그룹 소유자)의 그룹들 중, 주어진 friendId들이 속한 (friendId, 그룹이름) 쌍을 조회.
     * groupTags 일괄 계산용 (N+1 방지).
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT m.friendId, g.name
            FROM FriendGroupMember m
            JOIN FriendGroup g ON m.groupId = g.id
            WHERE g.userId = :viewerId
              AND m.friendId IN :friendIds
            """)
    List<Object[]> findFriendGroupNames(
            @org.springframework.data.repository.query.Param("viewerId") Long viewerId,
            @org.springframework.data.repository.query.Param("friendIds") java.util.Collection<Long> friendIds);

    /**
     * 그룹 id들의 멤버 수를 한 번에 집계 (목록 화면 memberCount용, N+1 방지).
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT m.groupId, COUNT(m)
            FROM FriendGroupMember m
            WHERE m.groupId IN :groupIds
            GROUP BY m.groupId
            """)
    List<Object[]> countByGroupIdIn(
            @org.springframework.data.repository.query.Param("groupIds") java.util.Collection<Long> groupIds);

    // 특정 그룹의 멤버 전체
    List<FriendGroupMember> findByGroupId(Long groupId);

    // 같은 그룹에 같은 친구 중복 추가 방지
    boolean existsByGroupIdAndFriendId(Long groupId, Long friendId);

    // 그룹에서 특정 멤버 제거용 조회
    java.util.Optional<FriendGroupMember> findByGroupIdAndFriendId(Long groupId, Long friendId);

    // 그룹 삭제 시 멤버 일괄 삭제
    void deleteByGroupId(Long groupId);
}