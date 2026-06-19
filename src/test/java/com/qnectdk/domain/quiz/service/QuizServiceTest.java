package com.qnectdk.domain.quiz.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.interest.service.InterestService;
import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.domain.quiz.client.QuizGenerationClient;
import com.qnectdk.domain.quiz.dto.QuizSaveRequest;
import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.port.PointPort;
import com.qnectdk.domain.quiz.repository.QuizAnswerRepository;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizOptionRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import com.qnectdk.domain.user.service.UserQueryService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * QuizService 단위 테스트(Mockito). 적립 연동(QUIZ_FIRST_SETUP 생애 1회 판정)과
 * 소프트 삭제 위임/예외 동작을 mock 으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizQuestionRepository questionRepository;
    @Mock
    private QuizOptionRepository optionRepository;
    @Mock
    private QuizAttemptRepository attemptRepository;
    @Mock
    private QuizAnswerRepository answerRepository;
    @Mock
    private QuizWriter quizWriter;
    @Mock
    private QuizContentValidator validator;
    @Mock
    private QuizGenerationClient generationClient;
    @Mock
    private PointPort pointPort;
    @Mock
    private PointService pointService;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private ProfileService profileService;
    @Mock
    private InterestService interestService;

    @InjectMocks
    private QuizService quizService;

    private static final Long OWNER_ID = 42L;
    private static final Long QUIZ_ID = 7L;

    private QuizSaveRequest sampleRequest() {
        // 내용은 validator(mock)가 통과시키므로 최소 형태면 충분하다(questions 는 non-null 이어야 함).
        return new QuizSaveRequest(List.of(
                new QuizSaveRequest.QuestionInput(QuestionType.OX, "질문", "O", true, List.of())));
    }

    private Quiz stubSaveAssemblePath() {
        Quiz quiz = org.mockito.Mockito.mock(Quiz.class);
        when(quiz.getId()).thenReturn(QUIZ_ID);
        when(quiz.getType()).thenReturn(QuizType.FIRST_MEET);
        when(quiz.isActive()).thenReturn(true);
        when(quizWriter.replaceActiveContent(anyLong(), any())).thenReturn(quiz);
        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(quiz));
        when(questionRepository.findByQuizIdOrderBySeqAsc(QUIZ_ID)).thenReturn(List.of());
        return quiz;
    }

    @Test
    @DisplayName("최초 설정(existsByOwnerId=false)이면 saveMyQuiz 시 QUIZ_FIRST_SETUP 1회 적립")
    void saveMyQuiz_firstSetup_earnsOnce() {
        when(quizRepository.existsByOwnerId(OWNER_ID)).thenReturn(false);
        stubSaveAssemblePath();

        quizService.saveMyQuiz(OWNER_ID, sampleRequest());

        verify(pointService).earn(
                eq(OWNER_ID), eq(PointPolicy.QUIZ_FIRST_SETUP), eq(PointReason.QUIZ_FIRST_SETUP), isNull());
    }

    @Test
    @DisplayName("이미 레코드 존재(existsByOwnerId=true)면 saveMyQuiz 시 적립 없음")
    void saveMyQuiz_existing_neverEarns() {
        when(quizRepository.existsByOwnerId(OWNER_ID)).thenReturn(true);
        stubSaveAssemblePath();

        quizService.saveMyQuiz(OWNER_ID, sampleRequest());

        verify(pointService, never()).earn(anyLong(), org.mockito.ArgumentMatchers.anyInt(), any(), any());
    }

    @Test
    @DisplayName("활성 퀴즈가 있으면 deleteMyQuiz 가 clearContentAndDeactivate 에 위임한다")
    void deleteMyQuiz_active_delegates() {
        Quiz quiz = Quiz.create(OWNER_ID, QuizType.FIRST_MEET);
        when(quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(OWNER_ID))
                .thenReturn(Optional.of(quiz));

        quizService.deleteMyQuiz(OWNER_ID);

        verify(quizWriter).clearContentAndDeactivate(quiz);
    }

    @Test
    @DisplayName("활성 퀴즈가 없으면 deleteMyQuiz 는 QUIZ_NOT_FOUND 예외")
    void deleteMyQuiz_noActive_throwsQuizNotFound() {
        when(quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.deleteMyQuiz(OWNER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.QUIZ_NOT_FOUND);

        verify(quizWriter, never()).clearContentAndDeactivate(any());
    }
}
