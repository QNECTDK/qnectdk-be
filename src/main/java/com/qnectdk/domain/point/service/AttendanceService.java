package com.qnectdk.domain.point.service;

import com.qnectdk.domain.point.entity.AttendanceCheck;
import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.repository.AttendanceCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * 현재 연속 출석일(streak). 가장 최근 출석일이 오늘이거나 어제면 그 지점부터 하루씩 끊김 없이 이어진 일수를 센다.
     * 마지막 출석이 그제 이전이면(연속 끊김) 0을 반환한다. 출석 기록이 없어도 0.
     */
    public int currentStreak(Long userId) {
      LocalDate today = LocalDate.now();
      List<LocalDate> dates = attendanceRepository.findCheckDatesDesc(userId, today);
      if (dates.isEmpty()) {
        return 0;
      }
      LocalDate cursor = dates.get(0); // 가장 최근 출석일
      if (cursor.isBefore(today.minusDays(1))) {
        return 0; // 마지막 출석이 어제보다 이전 → 연속 끊김
      }
      int streak = 1;
      for (int i = 1; i < dates.size(); i++) {
        LocalDate date = dates.get(i);
        if (date.equals(cursor)) {
          continue; // 중복 방어(유니크 제약상 없겠지만)
        }
        if (date.equals(cursor.minusDays(1))) {
          streak++;
          cursor = date;
        } else {
          break; // 연속 끊김
        }
      }
      return streak;
    }
}