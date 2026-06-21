package com.qnectdk.domain.friend.service;

import com.qnectdk.domain.friend.dto.FriendCardResponse;
import com.qnectdk.domain.friend.dto.FriendResponse;
import com.qnectdk.domain.friend.dto.FriendSummary;
import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.repository.FriendshipRepository;
import com.qnectdk.domain.notification.entity.NotificationType;
import com.qnectdk.domain.notification.service.NotificationService;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.service.PersonCardService;
import com.qnectdk.domain.reminder.service.ReminderService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 친구 도메인(방향성 "명함첩" 모델).
 * - QR/URL로 상대 프로필 접속 → '수락' 시 {@link #addFriend} 로 (나→상대)·(상대→나) 두 행을 함께 만들어
 * 상호 친구가 된다.
 * - '거절'은 클라이언트 동작(팝업 닫기)일 뿐 서버 호출이 없다(보류 상태 없음).
 * - 삭제는 {@link #delete} 로 내 방향 행만 지운다(상대 무영향).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final PersonCardService personCardService;
    private final PointService pointService;
    private final ReminderService reminderService;
    private final NotificationService notificationService;

    /**
     * 친구 추가(수락). 상호 등록 + 첫 만남 퀴즈 알림 발송 + 양쪽 30일 리마인드 예약 + 친구수 마일스톤.
     * 멱등: 이미 있는 방향은 그대로 둔다(중복 행 생성 안 함).
     */
    @Transactional
    public FriendResponse addFriend(Long meId, Long friendId) {
      if (meId.equals(friendId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Friendship mine = saveIfAbsent(meId, friendId);
        Friendship theirs = saveIfAbsent(friendId, meId);

        LocalDateTime now = LocalDateTime.now();
        // 양쪽 30일 리마인드 예약 (각자 자기 방향 행 기준)
        reminderService.scheduleReminder(mine.getId(), meId, now);
        reminderService.scheduleReminder(theirs.getId(), friendId, now);

        // 친구 수 마일스톤 — 양쪽
        pointService.earnFriendMilestone(meId, getFriendIds(meId).size());
        pointService.earnFriendMilestone(friendId, getFriendIds(friendId).size());

        // 첫 만남 퀴즈 알림 — 양쪽에게(상대 프로필의 '퀴즈 풀기'로 진입). refId=상대 userId
        notificationService.push(friendId, NotificationType.FRIEND_ADD,
            "새로운 친구가 추가되었습니다!", "첫 만남 퀴즈를 풀어보세요!", meId);
        notificationService.push(meId, NotificationType.FRIEND_ADD,
            "새로운 친구가 추가되었습니다!", "첫 만남 퀴즈를 풀어보세요!", friendId);

        return FriendResponse.from(mine);
      }

    /**
     * 친구 삭제(내 방향만). 상대의 (상대→나) 행은 그대로 두어 상대 목록엔 영향이 없다.
     */
    @Transactional
    public void delete(Long meId, Long friendId) {
      Friendship f = friendshipRepository.findByOwnerIdAndFriendId(meId, friendId)
          .orElseThrow(() -> new BusinessException(ErrorCode.FRIENDSHIP_NOT_FOUND));
      friendshipRepository.delete(f);
    }

    /** 내 친구 목록(방향성). */
    public List<FriendResponse> getFriends(Long userId) {
      return friendshipRepository.findByOwnerId(userId).stream()
                .map(FriendResponse::from)
                .toList();
    }

    /** 내 친구 userId 목록(데일리 통계·친구 퀴즈 등 경계 조회용). */
    public List<Long> getFriendIds(Long userId) {
      return friendshipRepository.findByOwnerId(userId).stream()
          .map(Friendship::getFriendId)
            .toList();
    }

    /** 내가 그 사람을 친구로 저장했는지(그룹 멤버 검증 등). */
    public boolean isFriend(Long ownerId, Long friendId) {
      return friendshipRepository.existsByOwnerIdAndFriendId(ownerId, friendId);
    }

    /** 내 친구 목록을 person 카드와 함께 반환. sort: "name"=이름순, 그 외=저장 최신순. */
    public List<FriendCardResponse> getFriendCards(Long userId, String sort) {
      List<Friendship> rows = friendshipRepository.findByOwnerId(userId);
      if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> friendIds = rows.stream().map(Friendship::getFriendId).toList();
        Map<Long, PersonCard> cardByUserId = personCardService.getCards(userId, friendIds).stream()
                .collect(Collectors.toMap(PersonCard::userId, c -> c));

        List<FriendCardResponse> cards = rows.stream()
            .map(f -> new FriendCardResponse(f.getId(), f.getCreatedAt(), cardByUserId.get(f.getFriendId())))
                .toList();

        Comparator<FriendCardResponse> comparator = "name".equalsIgnoreCase(sort)
                ? Comparator.comparing(c -> c.person() == null ? "" : c.person().name())
                : Comparator.comparing(FriendCardResponse::savedAt).reversed();
        return cards.stream().sorted(comparator).toList();
    }

    /** 자동완성용 친구 목록(id+이름+캐릭터). */
    public List<FriendSummary> getFriendSummaries(Long userId) {
      List<Long> friendIds = getFriendIds(userId);
        if (friendIds.isEmpty()) {
            return List.of();
          }
        return personCardService.getCards(userId, friendIds).stream()
                .map(c -> new FriendSummary(c.userId(), c.name(), c.characterId()))
                .toList();
    }

    /**
     * 내 방향 친구관계 행(friendshipId)에서 상대 userId 를 찾는다(리마인드 카드 해석용).
     * 내가 owner 인 행만 인정한다(삭제됐으면 비어있음).
     */
    public Optional<Long> findCounterpartUserId(Long friendshipId, Long userId) {
        return friendshipRepository.findById(friendshipId)
            .filter(f -> f.getOwnerId().equals(userId))
            .map(Friendship::getFriendId);
    }

    private Friendship saveIfAbsent(Long ownerId, Long friendId) {
      return friendshipRepository.findByOwnerIdAndFriendId(ownerId, friendId)
          .orElseGet(() -> friendshipRepository.save(Friendship.of(ownerId, friendId)));
    }
  }
