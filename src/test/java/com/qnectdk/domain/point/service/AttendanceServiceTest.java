package com.qnectdk.domain.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.point.repository.AttendanceCheckRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AttendanceService 연속 출석일(streak) 계산 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceCheckRepository attendanceRepository;
    @Mock
    private PointService pointService;

    private AttendanceService service() {
        return new AttendanceService(attendanceRepository, pointService);
    }

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("출석 기록이 없으면 streak 0")
    void noRecords_zero() {
        when(attendanceRepository.findCheckDatesDesc(eqUser(), today())).thenReturn(List.of());
        assertThat(service().currentStreak(USER_ID)).isZero();
    }

    @Test
    @DisplayName("오늘부터 3일 연속이면 streak 3")
    void threeConsecutive_three() {
        LocalDate today = LocalDate.now();
        when(attendanceRepository.findCheckDatesDesc(USER_ID, today))
                .thenReturn(List.of(today, today.minusDays(1), today.minusDays(2)));
        assertThat(service().currentStreak(USER_ID)).isEqualTo(3);
    }

    @Test
    @DisplayName("마지막 출석이 어제면(오늘 미출석) 이어진 일수를 그대로 센다")
    void endsYesterday_counted() {
        LocalDate today = LocalDate.now();
        when(attendanceRepository.findCheckDatesDesc(USER_ID, today))
                .thenReturn(List.of(today.minusDays(1), today.minusDays(2)));
        assertThat(service().currentStreak(USER_ID)).isEqualTo(2);
    }

    @Test
    @DisplayName("마지막 출석이 그제 이전이면 연속 끊김 → streak 0")
    void brokenStreak_zero() {
        LocalDate today = LocalDate.now();
        when(attendanceRepository.findCheckDatesDesc(USER_ID, today))
                .thenReturn(List.of(today.minusDays(2), today.minusDays(3)));
        assertThat(service().currentStreak(USER_ID)).isZero();
    }

    @Test
    @DisplayName("중간에 비면 거기서 끊어 센다")
    void gapBreaks() {
        LocalDate today = LocalDate.now();
        when(attendanceRepository.findCheckDatesDesc(USER_ID, today))
                .thenReturn(List.of(today, today.minusDays(2), today.minusDays(3)));
        assertThat(service().currentStreak(USER_ID)).isEqualTo(1);
    }

    private Long eqUser() {
        return USER_ID;
    }

    private LocalDate today() {
        return LocalDate.now();
    }
}
