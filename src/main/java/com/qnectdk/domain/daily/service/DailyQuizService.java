package com.qnectdk.domain.daily.service;

import com.qnectdk.domain.daily.dto.DailyAnswerRequest;
import com.qnectdk.domain.daily.dto.DailyStatsResponse;
import com.qnectdk.domain.daily.dto.DailyStatsResponse.Distribution;
import com.qnectdk.domain.daily.dto.DailyStatsResponse.FriendChoice;
import com.qnectdk.domain.daily.dto.DailyStatsResponse.FriendStats;
import com.qnectdk.domain.daily.dto.DailyTodayResponse;
import com.qnectdk.domain.daily.entity.DailyChoice;
import com.qnectdk.domain.daily.entity.DailyQuiz;
import com.qnectdk.domain.daily.entity.DailyQuizAnswer;
import com.qnectdk.domain.daily.port.FriendQueryPort;
import com.qnectdk.domain.daily.repository.DailyQuizAnswerRepository;
import com.qnectdk.domain.daily.repository.DailyQuizRepository;
import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.service.PersonCardService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 데일리 밸런스 게임. 오늘의 질문 조회/답변/통계.
 * 핵심 규칙: "내가 답해야 결과(비율) 공개" — 미답변 시 통계는 숨긴다.
 * 친구 ID 목록은 friend 도메인(B) 경계 포트로 조회한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyQuizService {

    // 하루 경계는 앱 기준 시간대(Asia/Seoul)로 판단한다(자정 갱신).
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final DailyQuizRepository dailyQuizRepository;
    private final DailyQuizAnswerRepository answerRepository;
    private final FriendQueryPort friendQueryPort;
    private final PersonCardService personCardService;
    private final PointService pointService;

    public DailyTodayResponse getToday(Long userId) {
        DailyQuiz quiz = getTodayQuizOrThrow();
        return answerRepository.findByDailyQuizIdAndUserId(quiz.getId(), userId)
                .map(answer -> DailyTodayResponse.answered(quiz, answer.getSelected()))
                .orElseGet(() -> DailyTodayResponse.notAnswered(quiz));
    }

    @Transactional
    public DailyStatsResponse submitAnswer(Long userId, DailyAnswerRequest request) {
        DailyQuiz quiz = getTodayQuizOrThrow();
        if (answerRepository.existsByDailyQuizIdAndUserId(quiz.getId(), userId)) {
            throw new BusinessException(ErrorCode.DAILY_ALREADY_ANSWERED);
        }
        answerRepository.save(DailyQuizAnswer.create(quiz.getId(), userId, request.selected()));
        // 데일리 답변 보상 +5P (refId=오늘 데일리 id → 하루 1회만 적립, 멱등)
        pointService.earn(userId, PointPolicy.DAILY_ANSWER, PointReason.DAILY_ANSWER, quiz.getId());
        return buildStats(quiz, userId);
    }

    public DailyStatsResponse getStats(Long userId) {
        DailyQuiz quiz = getTodayQuizOrThrow();
        if (!answerRepository.existsByDailyQuizIdAndUserId(quiz.getId(), userId)) {
            throw new BusinessException(ErrorCode.DAILY_NOT_ANSWERED_YET);
        }
        return buildStats(quiz, userId);
    }

    private DailyStatsResponse buildStats(DailyQuiz quiz, Long userId) {
        Distribution overall = Distribution.of(
                answerRepository.countByDailyQuizIdAndSelected(quiz.getId(), DailyChoice.A),
                answerRepository.countByDailyQuizIdAndSelected(quiz.getId(), DailyChoice.B));

        List<Long> friendIds = friendQueryPort.findFriendIds(userId);
        List<DailyQuizAnswer> friendAnswers = friendIds.isEmpty()
                ? List.of()
                : answerRepository.findByDailyQuizIdAndUserIdIn(quiz.getId(), friendIds);

        long friendA = friendAnswers.stream().filter(answer -> answer.getSelected() == DailyChoice.A).count();
        long friendB = friendAnswers.stream().filter(answer -> answer.getSelected() == DailyChoice.B).count();

        // 답한 친구의 이름·캐릭터를 한 번의 batch 로 조회(루프 내 단건 조회 N+1 방지).
        List<Long> answeredFriendIds = friendAnswers.stream().map(DailyQuizAnswer::getUserId).toList();
        Map<Long, PersonCard> cardByUserId = personCardService.getCards(userId, answeredFriendIds).stream()
                .collect(Collectors.toMap(PersonCard::userId, card -> card));
        List<FriendChoice> selections = friendAnswers.stream()
                .map(answer -> {
                    PersonCard card = cardByUserId.get(answer.getUserId());
                    return new FriendChoice(
                            answer.getUserId(),
                            card != null ? card.name() : "",
                            card != null ? card.characterId() : null,
                            answer.getSelected());
                })
                .toList();

        Distribution friendDistribution = Distribution.of(friendA, friendB);
        FriendStats friends = new FriendStats(
                friendDistribution.countA(), friendDistribution.countB(), friendDistribution.total(),
                friendDistribution.percentA(), friendDistribution.percentB(), selections);

        return new DailyStatsResponse(quiz.getId(), overall, friends);
    }

    private DailyQuiz getTodayQuizOrThrow() {
        return dailyQuizRepository.findByQuizDate(LocalDate.now(ZONE))
                .orElseThrow(() -> new BusinessException(ErrorCode.DAILY_QUIZ_NOT_FOUND));
    }
}
