package com.qnectdk.domain.point.repository;

import com.qnectdk.domain.point.entity.AttendanceCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheck, Long> {

    boolean existsByUserIdAndCheckDate(Long userId, LocalDate checkDate);
}