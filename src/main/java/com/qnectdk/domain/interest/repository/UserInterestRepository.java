package com.qnectdk.domain.interest.repository;

import com.qnectdk.domain.interest.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByUserId(Long userId);
    List<UserInterest> findByUserIdIn(java.util.Collection<Long> userIds);

    void deleteByUserId(Long userId);
}
