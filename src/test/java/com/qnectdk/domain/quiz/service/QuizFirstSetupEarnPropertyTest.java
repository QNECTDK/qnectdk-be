package com.qnectdk.domain.quiz.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.quiz.dto.QuizSaveRequest;
import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.repository.QuizAnswerRepository;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import java.util.List;
import java.util.Optional;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;

/**
 * Property 2: 퀴즈 최초 설정 생애 1회 적립 (P2).
 *
 * <p>jqwik 은 자체 라이프사이클을 쓰므로 Mockito mock 을 메서드 안에서 직접 만들어 사용한다.
 */
class QuizFirstSetupEarnPropertyTest {

    private static final Long QUIZ_ID = 100L;

    private QuizSaveRequest sampleRequest() {
        return new QuizSaveRequest(List.of(
                new QuizSaveRequest.QuestionInput(QuestionType.OX, "질문", "O", true, List.of())));
    }

    private Quiz stubbedQuiz() {
        Quiz quiz = mock(Quiz.class);
        when(quiz.getId()).thenReturn(QUIZ_ID);
        when(quiz.getType()).thenReturn(QuizType.FIRST_MEET);
        when(quiz.isActive()).thenReturn(true);
        return quiz;
    }

    // Feature: point-notification-integration, Property 2: 퀴즈 최초 설정 생애 1회 적립
    // 임의의 ownerId 와 호출 시퀀스에 대해, 최초(existsByOwnerId=false)일 때만 earn 1회,
    // 이미 레코드가 존재하면(재저장/삭제 후 재설정 모두 existsByOwnerId=true) earn 0회다.
    // Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5
    @Property(tries = 100)
    void quizFirstSetupEarnsExactlyOnceOverLifetime(
            @ForAll @LongRange(min = 1, max = 1_000_000) Long ownerId,
            @ForAll @IntRange(min = 1, max = 10) int callCount,
            @ForAll boolean alreadyHadRecord) {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizQuestionRepository questionRepository = mock(QuizQuestionRepository.class);
        QuizAttemptRepository attemptRepository = mock(QuizAttemptRepository.class);
        QuizAnswerRepository answerRepository = mock(QuizAnswerRepository.class);
        QuizWriter quizWriter = mock(QuizWriter.class);
        PointService pointService = mock(PointService.class);

        Quiz quiz = stubbedQuiz();
        when(quizWriter.replaceActiveContent(anyLong(), any())).thenReturn(quiz);
        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(quiz));
        when(questionRepository.findByQuizIdOrderBySeqAsc(QUIZ_ID)).thenReturn(List.of());

        // 최초 판정은 "저장 직전 레코드 존재 여부". 이미 레코드가 있으면 항상 true(=적립 0회).
        // 없으면 첫 호출만 false, 첫 저장으로 레코드가 생기므로 이후는 true(=적립 1회).
        if (alreadyHadRecord) {
            when(quizRepository.existsByOwnerId(ownerId)).thenReturn(true);
        } else {
            when(quizRepository.existsByOwnerId(ownerId)).thenReturn(false, true);
        }

        QuizService quizService = QuizServiceMocks.create(
                quizRepository, questionRepository, attemptRepository, answerRepository,
                quizWriter, pointService);

        for (int i = 0; i < callCount; i++) {
            quizService.saveMyQuiz(ownerId, sampleRequest());
        }

        int expectedEarns = alreadyHadRecord ? 0 : 1;
        verify(pointService, times(expectedEarns))
                .earn(eq(ownerId), eq(PointPolicy.QUIZ_FIRST_SETUP), eq(PointReason.QUIZ_FIRST_SETUP), isNull());
        // 다른 인자 조합으로의 적립은 없어야 한다.
        verify(pointService, times(expectedEarns)).earn(anyLong(), anyInt(), any(), any());
    }
}
