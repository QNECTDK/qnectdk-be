package com.qnectdk.domain.group.repository;

import com.qnectdk.domain.group.entity.FriendGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendGroupRepository extends JpaRepository<FriendGroup, Long> {

    // 내 그룹 전체 목록
    List<FriendGroup> findByUserId(Long userId);

    // 그룹명 검색 (내 그룹 중 이름에 키워드 포함, 대소문자 무시)
    List<FriendGroup> findByUserIdAndNameContainingIgnoreCase(Long userId, String keyword);

    // 같은 유저가 같은 이름 그룹 중복 생성 방지용
    boolean existsByUserIdAndName(Long userId, String name);

    // 무료 5개 제한 체크용
    long countByUserId(Long userId);

}