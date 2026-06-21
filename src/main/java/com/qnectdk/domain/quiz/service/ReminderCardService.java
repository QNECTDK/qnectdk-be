package com.qnectdk.domain.quiz.service;

import com.qnectdk.domain.friend.service.FriendService;
import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.service.PersonCardService;
import com.qnectdk.domain.quiz.dto.ReminderCardResponse;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import com.qnectdk.domain.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 홈 "이 사람을 기억하나요?" 리마인드 카드 조립(복습 대상 친구 1명).
 *
 * <p>{@link QuizService} 와 분리한 이유: reminder/friend 가 {@code QuizService} 와 의존 사이클을 이루므로
 * (friend→reminder→quiz) 여기에 묶으면 순환이 된다. 이 빈은 누구도 의존하지 않아 안전하게 조립만 한다.
 *
 * <p>대상 선정: 받는 사람(나)에게 도래한 리마인드(scheduledAt&lt;=now) 중 최신 예정 건의 상대 친구.
 * 친구 관계가 해석되는 첫 대상을 카드로 만들고, 그 친구의 활성 퀴즈를 함께 실어 풀기로 이어준다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReminderCardService {

    private final ReminderService reminderService;
    private final FriendService friendService;
    private final PersonCardService personCardService;
    private final QuizRepository quizRepository;

    /** 오늘 복습 대상 카드. 대상이 없으면 null. */
    public ReminderCardResponse getTodayCard(Long userId) {
        List<Long> dueFriendshipIds = reminderService.findDueFriendshipIds(userId);
        for (Long friendshipId : dueFriendshipIds) {
            Long friendId = friendService.findCounterpartUserId(friendshipId, userId).orElse(null);
            if (friendId == null) {
                continue; // 관계가 끊겼거나 해석 불가 → 다음 후보
            }
            PersonCard person = personCardService.getCard(userId, friendId);
            if (person == null) {
                continue;
            }
            Long quizId = quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(friendId)
                    .map(Quiz::getId)
                    .orElse(null);
            return ReminderCardResponse.of(person, quizId);
        }
        return null;
    }
}
