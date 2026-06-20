package com.qnectdk.domain.daily.init;

import com.qnectdk.domain.daily.entity.DailyQuiz;
import com.qnectdk.domain.daily.repository.DailyQuizRepository;
import com.qnectdk.domain.notification.entity.NotificationType;
import com.qnectdk.domain.notification.service.NotificationService;
import com.qnectdk.domain.user.service.UserQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * DailyQuizSeeder 단위 테스트(Mockito).
 * 신규 생성 시 전체 사용자 알림 팬아웃, 이미 존재 시 무발송을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class DailyQuizSeederTest {

    private static final String EXPECTED_TITLE = "오늘의 퀴즈가 생성되었습니다!";
    private static final String EXPECTED_BODY = "지금 바로 풀어보세요!";
    private static final Long DAILY_QUIZ_ID = 42L;

    @Mock
    private DailyQuizRepository dailyQuizRepository;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DailyQuizSeeder seeder;

    @Test
    @DisplayName("사용자 N명 + 오늘 데일리 없음 → 각 사용자에게 push 정확히 N회, 인자 검증")
    void notifiesAllUsersOnNewCreation() {
        List<Long> userIds = List.of(11L, 22L, 33L);
        given(dailyQuizRepository.findByQuizDate(any(LocalDate.class))).willReturn(Optional.empty());
        DailyQuiz saved = mock(DailyQuiz.class);
        given(saved.getId()).willReturn(DAILY_QUIZ_ID);
        given(dailyQuizRepository.save(any(DailyQuiz.class))).willReturn(saved);
        given(userQueryService.getAllUserIds()).willReturn(userIds);

        seeder.run(null);

        verify(userQueryService).getAllUserIds();
        verify(notificationService, times(userIds.size()))
                .push(anyLong(), eq(NotificationType.DAILY_QUIZ), eq(EXPECTED_TITLE), eq(EXPECTED_BODY), eq(DAILY_QUIZ_ID));

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(notificationService, times(userIds.size()))
                .push(userIdCaptor.capture(), any(), anyString(), anyString(), anyLong());
        assertThat(userIdCaptor.getAllValues()).containsExactlyElementsOf(userIds);
    }

    @Test
    @DisplayName("오늘 데일리가 이미 존재 → save·push 모두 호출하지 않고 즉시 종료")
    void skipsWhenTodayDailyAlreadyExists() {
        given(dailyQuizRepository.findByQuizDate(any(LocalDate.class)))
                .willReturn(Optional.of(mock(DailyQuiz.class)));

        seeder.run(null);

        verify(dailyQuizRepository, never()).save(any());
        verify(userQueryService, never()).getAllUserIds();
        verify(notificationService, never()).push(anyLong(), any(), anyString(), anyString(), anyLong());
    }
}
