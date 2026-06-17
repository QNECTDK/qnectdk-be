package com.qnectdk.domain.group.repository;

import com.qnectdk.domain.group.entity.FriendGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMember, Long> {
}