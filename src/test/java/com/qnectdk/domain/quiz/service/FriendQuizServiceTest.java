package com.qnectdk.domain.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.service.PersonCardService;
import com.qnectdk.domain.quiz.dto.FriendQuizResponse;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizAttempt;
import com.qnectdk.domain.quiz.port.FriendQueryPort;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FriendQuizService 단위 테스트(Mockito). 친구별 활성 퀴즈 유무·문항수·최근 응시 조립과
 * 활성 퀴즈가 없는 친구의 hasQuiz=false 포함을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class FriendQuizServiceTest {

    @Mock
    private FriendQueryPort friendQueryPort;
    @Mock
    private PersonCardService personCardService;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizQuestionRepository questionRepository;
    @Mock
    private QuizAttemptRepository attemptRepository;

    @InjectMocks
    private FriendQuizService friendQuizService;

    private static final Long VIEWER_ID = 1L;

    private PersonCard person(Long userId) {
        return new PersonCard(userId, "친구" + userId, "character01", "국민대",
                "MALE", 2005, "ENFP", List.of(), List.of());
    }

    private Quiz activeQuiz(Long quizId, Long ownerId) {
        Quiz quiz = mock(Quiz.class);
        lenient().when(quiz.getId()).thenReturn(quizId);
        lenient().when(quiz.getOwnerId()).thenReturn(ownerId);
        return quiz;
    }

    private QuizQuestionRepository.QuizQuestionCount count(Long quizId, long cnt) {
        return new QuizQuestionRepository.QuizQuestionCount() {
            @Override
            public Long getQuizId() {
                return quizId;
            }

            @Override
            public long getCount() {
                return cnt;
            }
        };
    }

    @Test
    @DisplayName("친구가 없으면 빈 목록을 반환한다")
    void noFriends_returnsEmpty() {
        when(friendQueryPort.findFriendIds(VIEWER_ID)).thenReturn(List.of());

        assertThat(friendQuizService.getFriendQuizzes(VIEWER_ID)).isEmpty();
    }

    @Test
    @DisplayName("활성 퀴즈+응시 친구는 점수/응시여부를, 퀴즈 없는 친구는 hasQuiz=false 를 담는다")
    void mixedFriends_assembledCorrectly() {
        Long withQuizAttempted = 10L;   // 활성 퀴즈 있고 응시함
        Long withQuizNotAttempted = 11L; // 활성 퀴즈 있지만 미응시
        Long withoutQuiz = 12L;          // 활성 퀴즈 없음
        List<Long> friendIds = List.of(withQuizAttempted, withQuizNotAttempted, withoutQuiz);

        when(friendQueryPort.findFriendIds(VIEWER_ID)).thenReturn(friendIds);
        when(personCardService.getCards(eq(VIEWER_ID), anyList()))
                .thenReturn(List.of(person(withQuizAttempted), person(withQuizNotAttempted), person(withoutQuiz)));

        Quiz quizA = activeQuiz(100L, withQuizAttempted);
        Quiz quizB = activeQuiz(101L, withQuizNotAttempted);
        when(quizRepository.findByOwnerIdInAndActiveTrue(friendIds)).thenReturn(List.of(quizA, quizB));

        when(questionRepository.countByQuizIds(anyList()))
                .thenReturn(List.of(count(100L, 4), count(101L, 5)));

        // viewer 가 quizA(100) 만 응시 — score 3
        QuizAttempt attempt = QuizAttempt.create(100L, VIEWER_ID, 3, 4);
        when(attemptRepository.findBySolverAndQuizIdsOrderByIdDesc(eq(VIEWER_ID), anyList()))
                .thenReturn(List.of(attempt));

        List<FriendQuizResponse> result = friendQuizService.getFriendQuizzes(VIEWER_ID);

        assertThat(result).hasSize(3);

        FriendQuizResponse attempted = result.get(0);
        assertThat(attempted.person().userId()).isEqualTo(withQuizAttempted);
        assertThat(attempted.hasQuiz()).isTrue();
        assertThat(attempted.quizId()).isEqualTo(100L);
        assertThat(attempted.totalQuestions()).isEqualTo(4);
        assertThat(attempted.attempted()).isTrue();
        assertThat(attempted.score()).isEqualTo(3);

        FriendQuizResponse notAttempted = result.get(1);
        assertThat(notAttempted.hasQuiz()).isTrue();
        assertThat(notAttempted.quizId()).isEqualTo(101L);
        assertThat(notAttempted.totalQuestions()).isEqualTo(5);
        assertThat(notAttempted.attempted()).isFalse();
        assertThat(notAttempted.score()).isNull();

        FriendQuizResponse noQuiz = result.get(2);
        assertThat(noQuiz.hasQuiz()).isFalse();
        assertThat(noQuiz.quizId()).isNull();
        assertThat(noQuiz.totalQuestions()).isZero();
        assertThat(noQuiz.attempted()).isFalse();
        assertThat(noQuiz.score()).isNull();
    }
}
