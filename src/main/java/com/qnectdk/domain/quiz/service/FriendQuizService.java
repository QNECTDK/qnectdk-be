package com.qnectdk.domain.quiz.service;

import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.service.PersonCardService;
import com.qnectdk.domain.quiz.dto.FriendQuizResponse;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizAttempt;
import com.qnectdk.domain.quiz.port.FriendQueryPort;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 친구 퀴즈 목록 조회 전용 서비스(화면 "퀴즈 풀기"). 친구별로 활성 퀴즈 유무·총문항·내 최근 응시 점수를 한 줄로 조립한다.
 *
 * <p>{@link QuizService} 와 분리한 이유: QuizService 는 reminder 도메인이 의존하므로
 * ({@code ReminderService → QuizService}) 여기서 friend 도메인을 끌어오면 순환 참조가 된다.
 * 이 서비스는 어떤 빈도 의존하지 않으므로 friend 경계({@link FriendQueryPort})를 안전하게 사용할 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendQuizService {

    private final FriendQueryPort friendQueryPort;
    private final PersonCardService personCardService;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;

    /**
     * 친구 퀴즈 목록. 활성 퀴즈가 없는 친구도 hasQuiz=false 로 포함한다.
     * 부가정보(person·문항수·최근 응시)는 모두 batch 조회로 N+1 을 피한다.
     */
    public List<FriendQuizResponse> getFriendQuizzes(Long viewerId) {
        List<Long> friendIds = friendQueryPort.findFriendIds(viewerId);
        if (friendIds.isEmpty()) {
            return List.of();
        }
        Map<Long, PersonCard> cardByUserId = personCardService.getCards(viewerId, friendIds).stream()
                .collect(Collectors.toMap(PersonCard::userId, card -> card));

        // owner 별 활성 퀴즈(정상 상태에선 1개, 혹시 복수면 최신 id 채택)
        Map<Long, Quiz> quizByOwner = quizRepository.findByOwnerIdInAndActiveTrue(friendIds).stream()
                .collect(Collectors.toMap(Quiz::getOwnerId, quiz -> quiz,
                        (a, b) -> a.getId() >= b.getId() ? a : b));
        List<Long> quizIds = quizByOwner.values().stream().map(Quiz::getId).toList();

        Map<Long, Integer> questionCountByQuiz = quizIds.isEmpty() ? Map.of()
                : questionRepository.countByQuizIds(quizIds).stream()
                .collect(Collectors.toMap(
                        QuizQuestionRepository.QuizQuestionCount::getQuizId,
                        count -> (int) count.getCount()));

        Map<Long, QuizAttempt> latestAttemptByQuiz = new HashMap<>();
        if (!quizIds.isEmpty()) {
            // 최신순(id desc) 정렬이므로 quizId 별 첫 항목(=최근 응시)만 채택
            attemptRepository.findBySolverAndQuizIdsOrderByIdDesc(viewerId, quizIds)
                    .forEach(attempt -> latestAttemptByQuiz.putIfAbsent(attempt.getQuizId(), attempt));
        }

        return friendIds.stream()
                .map(friendId -> {
                    PersonCard person = cardByUserId.get(friendId);
                    Quiz quiz = quizByOwner.get(friendId);
                    if (quiz == null) {
                        return FriendQuizResponse.noQuiz(person);
                    }
                    int totalQuestions = questionCountByQuiz.getOrDefault(quiz.getId(), 0);
                    return FriendQuizResponse.of(
                            person, quiz.getId(), totalQuestions, latestAttemptByQuiz.get(quiz.getId()));
                })
                .toList();
    }
}
