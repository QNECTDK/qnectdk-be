package com.qnectdk.domain.point.repository;

import com.qnectdk.domain.point.entity.AttendanceCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheck, Long> {

    boolean existsByUserIdAndCheckDate(Long userId, LocalDate checkDate);

    // 연속 출석일(streak) 계산용 — 최신 출석일부터 내림차순. 오늘 이전(<=오늘) 기록만.
    @Query("select a.checkDate from AttendanceCheck a "
        + "where a.userId = :userId and a.checkDate <= :today order by a.checkDate desc")
    List<LocalDate> findCheckDatesDesc(@Param("userId") Long userId, @Param("today") LocalDate today);
}