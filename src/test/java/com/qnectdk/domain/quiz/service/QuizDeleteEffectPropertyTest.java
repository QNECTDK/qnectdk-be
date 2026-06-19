package com.qnectdk.domain.quiz.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import java.util.Optional;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

/**
 * Property 6: 퀴즈 삭제 효과 (P6).
 */
class QuizDeleteEffectPropertyTest {

    private QuizService newService(QuizRepository quizRepository, QuizWriter quizWriter) {
        return QuizServiceMocks.create(
                quizRepository,
                mock(QuizQuestionRepository.class),
                mock(QuizAttemptRepository.class),
                mock(QuizAnswerRepository.class),
                quizWriter,
                mock(PointService.class));
    }

    // Feature: point-notification-integration, Property 6: 퀴즈 삭제 효과
    // 임의의 사용자에게 활성 퀴즈가 존재하면 deleteMyQuiz → quizWriter.clearContentAndDeactivate(해당 quiz)
    // 호출로 삭제 효과를 위임한다. 조회·위임 모두 요청 ownerId 로만 수행되어 다른 사용자에 영향이 없다.
    // Validates: Requirements 4.2, 4.4, 4.8
    @Property(tries = 100)
    void deleteActiveQuizDelegatesToClearForRequestedOwner(
            @ForAll @LongRange(min = 1, max = 1_000_000) Long ownerId) {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizWriter quizWriter = mock(QuizWriter.class);
        Quiz quiz = Quiz.create(ownerId, QuizType.FIRST_MEET);
        when(quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId))
                .thenReturn(Optional.of(quiz));

        QuizService quizService = newService(quizRepository, quizWriter);

        quizService.deleteMyQuiz(ownerId);

        // 요청 ownerId 로만 조회하고, 그 사용자의 활성 퀴즈에 대해서만 위임한다(ownerId 매칭).
        verify(quizRepository).findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId);
        verify(quizWriter).clearContentAndDeactivate(quiz);
    }

    // Feature: point-notification-integration, Property 6: 퀴즈 삭제 효과
    // 활성 퀴즈가 없으면 QUIZ_NOT_FOUND 로 응답하고 어떤 삭제 위임도 일어나지 않는다.
    // Validates: Requirements 4.2, 4.4, 4.8
    @Property(tries = 100)
    void deleteWithoutActiveQuizThrowsNotFound(
            @ForAll @LongRange(min = 1, max = 1_000_000) Long ownerId) {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizWriter quizWriter = mock(QuizWriter.class);
        when(quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId))
                .thenReturn(Optional.empty());

        QuizService quizService = newService(quizRepository, quizWriter);

        assertThatThrownBy(() -> quizService.deleteMyQuiz(ownerId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND);

        verify(quizWriter, never()).clearContentAndDeactivate(any());
    }
}
