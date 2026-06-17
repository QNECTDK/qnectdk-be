package com.qnectdk.domain.interest.repository;

import com.qnectdk.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    List<Interest> findAllByOrderByCategoryAscNameAsc();
}
