package com.qnectdk.domain.daily.init;

import com.qnectdk.domain.daily.entity.DailyQuiz;
import com.qnectdk.domain.daily.repository.DailyQuizRepository;
import com.qnectdk.domain.notification.entity.NotificationType;
import com.qnectdk.domain.notification.service.NotificationService;
import com.qnectdk.domain.user.service.UserQueryService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * DailyQuizSeeder 속성 기반 테스트(jqwik). B 서비스는 mock으로 대체한다.
 */
class DailyQuizSeederPropertyTest {

    private static final String EXPECTED_TITLE = "오늘의 퀴즈가 생성되었습니다!";
    private static final String EXPECTED_BODY = "지금 바로 풀어보세요!";
    private static final Long DAILY_QUIZ_ID = 777L;

    // Feature: point-notification-integration, Property 3: 데일리 알림 팬아웃 정확성
    // 임의 사용자 ID 목록(크기 N>=0)에 대해 신규 생성 시 push 정확히 N회(각 id로 1회씩, 중복/누락 없음), N=0이면 0회.
    // Validates: Requirements 3.1, 3.2, 3.3, 3.6
    @Property(tries = 100)
    void fansOutExactlyOncePerUser(
            @ForAll @Size(max = 50) List<@IntRange(min = 1, max = 100_000) Integer> rawIds) {
        List<Long> userIds = rawIds.stream().map(Integer::longValue).toList();

        DailyQuizRepository dailyQuizRepository = mock(DailyQuizRepository.class);
        UserQueryService userQueryService = mock(UserQueryService.class);
        NotificationService notificationService = mock(NotificationService.class);

        given(dailyQuizRepository.findByQuizDate(any(LocalDate.class))).willReturn(Optional.empty());
        DailyQuiz saved = mock(DailyQuiz.class);
        given(saved.getId()).willReturn(DAILY_QUIZ_ID);
        given(dailyQuizRepository.save(any(DailyQuiz.class))).willReturn(saved);
        given(userQueryService.getAllUserIds()).willReturn(userIds);

        DailyQuizSeeder seeder = new DailyQuizSeeder(dailyQuizRepository, userQueryService, notificationService);
        seeder.run(null);

        // 호출 횟수 = 목록 크기 (N=0이면 0회)
        verify(notificationService, times(userIds.size()))
                .push(anyLong(), any(), anyString(), anyString(), anyLong());
        // 각 사용자에게 정확한 인자로 1회씩 (중복/누락 없음)
        for (Long userId : userIds.stream().distinct().toList()) {
            long occurrences = userIds.stream().filter(id -> id.equals(userId)).count();
            verify(notificationService, times((int) occurrences))
                    .push(eq(userId), eq(NotificationType.DAILY_QUIZ),
                            eq(EXPECTED_TITLE), eq(EXPECTED_BODY), eq(DAILY_QUIZ_ID));
        }
    }

    // Feature: point-notification-integration, Property 4: 재실행 무발송
    // 오늘 데일리가 이미 존재(findByQuizDate present)면 임의 사용자 목록과 무관하게 save·push 0회.
    // Validates: Requirements 3.5
    @Property(tries = 100)
    void noSendWhenAlreadyExists(
            @ForAll @Size(max = 50) List<@IntRange(min = 1, max = 100_000) Integer> rawIds) {
        DailyQuizRepository dailyQuizRepository = mock(DailyQuizRepository.class);
        UserQueryService userQueryService = mock(UserQueryService.class);
        NotificationService notificationService = mock(NotificationService.class);

        given(dailyQuizRepository.findByQuizDate(any(LocalDate.class)))
                .willReturn(Optional.of(mock(DailyQuiz.class)));

        DailyQuizSeeder seeder = new DailyQuizSeeder(dailyQuizRepository, userQueryService, notificationService);
        seeder.run(null);

        verify(dailyQuizRepository, never()).save(any());
        verifyNoInteractions(userQueryService);
        verifyNoInteractions(notificationService);
    }

    // Feature: point-notification-integration, Property 5: 발송 실패 격리
    // 임의 사용자 목록과 그 중 임의 부분집합에서 push가 예외를 던져도, 시더는 목록 전체에 push를 시도(호출 횟수 = 목록 크기)하고
    // run()이 예외를 전파하지 않는다.
    // Validates: Requirements 3.7
    @Property(tries = 100)
    void isolatesPushFailures(
            @ForAll @Size(max = 50) @UniqueElements List<@IntRange(min = 1, max = 100_000) Integer> rawIds,
            @ForAll Set<@IntRange(min = 1, max = 100_000) Integer> rawFailing) {
        List<Long> userIds = rawIds.stream().map(Integer::longValue).toList();
        Set<Long> failing = rawFailing.stream().map(Integer::longValue).collect(java.util.stream.Collectors.toSet());

        DailyQuizRepository dailyQuizRepository = mock(DailyQuizRepository.class);
        UserQueryService userQueryService = mock(UserQueryService.class);
        NotificationService notificationService = mock(NotificationService.class);

        given(dailyQuizRepository.findByQuizDate(any(LocalDate.class))).willReturn(Optional.empty());
        DailyQuiz saved = mock(DailyQuiz.class);
        given(saved.getId()).willReturn(DAILY_QUIZ_ID);
        given(dailyQuizRepository.save(any(DailyQuiz.class))).willReturn(saved);
        given(userQueryService.getAllUserIds()).willReturn(userIds);

        // 실패 부분집합에 속한 사용자의 push는 예외를 던진다.
        doAnswer(invocation -> {
            Long uid = invocation.getArgument(0);
            if (failing.contains(uid)) {
                throw new RuntimeException("푸시 발송 실패 userId=" + uid);
            }
            return null;
        }).when(notificationService).push(anyLong(), any(), anyString(), anyString(), anyLong());

        DailyQuizSeeder seeder = new DailyQuizSeeder(dailyQuizRepository, userQueryService, notificationService);

        // run()은 예외를 전파하지 않는다.
        assertThatCode(() -> seeder.run(null)).doesNotThrowAnyException();

        // 실패 여부와 무관하게 목록 전체에 대해 push 시도(호출 횟수 = 목록 크기).
        verify(notificationService, times(userIds.size()))
                .push(anyLong(), any(), anyString(), anyString(), anyLong());
        // 데일리 저장은 발생(롤백 트리거 없음).
        verify(dailyQuizRepository).save(any(DailyQuiz.class));
        assertThat(userIds).hasSize(rawIds.size());
    }
}
