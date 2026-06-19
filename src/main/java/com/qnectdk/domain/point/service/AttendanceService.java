package com.qnectdk.domain.point.service;

import com.qnectdk.domain.point.entity.AttendanceCheck;
import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.repository.AttendanceCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceCheckRepository attendanceRepository;
    private final PointService pointService;

    /**
     * 출석 체크. 하루 1회만 5P 지급.
     * @return true=오늘 처음 출석(지급함), false=오늘 이미 출석함(지급 안 함)
     */
    @Transactional
    public boolean checkToday(Long userId) {
        LocalDate today = LocalDate.now();

        // 오늘 이미 출석했으면 지급 안 함
        if (attendanceRepository.existsByUserIdAndCheckDate(userId, today)) {
            return false;
        }

        // 출석 기록 + 포인트 지급
        attendanceRepository.save(AttendanceCheck.of(userId, today));
        pointService.earn(userId, PointPolicy.ATTENDANCE, PointReason.ATTENDANCE, null);
        return true;
    }
}