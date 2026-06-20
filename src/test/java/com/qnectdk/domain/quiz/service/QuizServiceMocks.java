package com.qnectdk.domain.quiz.service;

import static org.mockito.Mockito.mock;

import com.qnectdk.domain.interest.service.InterestService;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.domain.quiz.client.QuizGenerationClient;
import com.qnectdk.domain.quiz.port.PointPort;
import com.qnectdk.domain.quiz.repository.QuizAnswerRepository;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizOptionRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import com.qnectdk.domain.user.service.UserQueryService;

/**
 * 속성 기반 테스트(jqwik)에서 {@link QuizService} 를 전부 mock 의존성으로 조립하기 위한 헬퍼.
 * jqwik 은 자체 라이프사이클을 쓰므로 {@code MockitoExtension} 대신 {@code Mockito.mock} 으로 직접 만든
 * 의존성을 생성자에 주입한다. 테스트가 관심 갖는 mock 만 인자로 받고 나머지는 내부에서 mock 으로 채운다.
 */
final class QuizServiceMocks {

    private QuizServiceMocks() {
    }

    static QuizService create(QuizRepository quizRepository,
                              QuizQuestionRepository questionRepository,
                              QuizAttemptRepository attemptRepository,
                              QuizAnswerRepository answerRepository,
                              QuizWriter quizWriter,
                              PointService pointService) {
        return new QuizService(
                quizRepository,
                questionRepository,
                mock(QuizOptionRepository.class),
                attemptRepository,
                answerRepository,
                quizWriter,
                mock(QuizContentValidator.class), // validate(...) 는 void → 기본 동작으로 통과
                mock(QuizGenerationClient.class),
                mock(PointPort.class),
                pointService,
                mock(UserQueryService.class),
                mock(ProfileService.class),
                mock(InterestService.class));
    }
}
