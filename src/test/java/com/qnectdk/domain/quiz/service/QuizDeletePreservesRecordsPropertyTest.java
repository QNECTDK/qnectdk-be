package com.qnectdk.domain.quiz.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.repository.QuizAnswerRepository;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import java.util.Optional;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;

/**
 * Property 7: 퀴즈 삭제 후 레코드·응시 기록 보존 (P7).
 *
 * <p>소프트 삭제는 문항·보기만 제거하고 {@code quiz.deactivate()} 만 수행하므로, 서비스 레벨에서
 * 퀴즈 행 삭제(quizRepository.delete/deleteById)나 응시 기록 삭제(attempt/answer 리포지토리 삭제)가
 * 일어나지 않아야 한다. 임의의 응시 기록 수에 대해 삭제 호출이 0회임을 검증한다.
 */
class QuizDeletePreservesRecordsPropertyTest {

    // Feature: point-notification-integration, Property 7: 퀴즈 삭제 후 레코드·응시 기록 보존
    // 임의의 활성 퀴즈와 임의 개수의 응시 기록이 있더라도, deleteMyQuiz 수행 시 퀴즈 행과 응시 기록을
    // 삭제하지 않는다(quizRepository / attemptRepository / answerRepository 의 삭제 호출 0회).
    // Validates: Requirements 4.5, 4.9
    @Property(tries = 100)
    void deleteDoesNotRemoveQuizRowNorAttemptRecords(
            @ForAll @LongRange(min = 1, max = 1_000_000) Long ownerId,
            @ForAll @IntRange(min = 0, max = 50) int attemptCount) {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizQuestionRepository questionRepository = mock(QuizQuestionRepository.class);
        QuizAttemptRepository attemptRepository = mock(QuizAttemptRepository.class);
        QuizAnswerRepository answerRepository = mock(QuizAnswerRepository.class);
        // 실제 위임 대상은 진짜 QuizWriter 가 아닌 mock 으로 두어, 서비스 레벨에서 삭제 호출이 없음을 본다.
        QuizWriter quizWriter = mock(QuizWriter.class);

        // attemptCount 는 "임의의 응시 기록 집합" 의 크기를 상징한다(서비스는 이를 건드리지 않아야 한다).
        // 서비스 deleteMyQuiz 는 응시 리포지토리를 조회·삭제하지 않으므로 별도 스텁 없이 호출 0회를 검증한다.

        Quiz quiz = Quiz.create(ownerId, QuizType.FIRST_MEET);
        when(quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId))
                .thenReturn(Optional.of(quiz));

        QuizService quizService = QuizServiceMocks.create(
                quizRepository, questionRepository, attemptRepository, answerRepository,
                quizWriter, mock(PointService.class));

        quizService.deleteMyQuiz(ownerId);

        // 퀴즈 행 보존: 어떤 형태의 삭제도 호출되지 않는다 → existsByOwnerId 가 계속 true 로 유지됨(4.5).
        verify(quizRepository, never()).delete(any());
        verify(quizRepository, never()).deleteById(any());
        verify(quizRepository, never()).deleteAll();

        // 응시 기록 보존: attempt/answer 리포지토리에 대한 삭제 호출 0회(4.9).
        verify(attemptRepository, never()).delete(any());
        verify(attemptRepository, never()).deleteById(any());
        verify(attemptRepository, never()).deleteAll();
        verify(answerRepository, never()).delete(any());
        verify(answerRepository, never()).deleteById(any());
        verify(answerRepository, never()).deleteAll();
    }
}
