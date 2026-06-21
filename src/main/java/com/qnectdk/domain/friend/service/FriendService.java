package com.qnectdk.domain.friend.service;

import com.qnectdk.domain.friend.dto.FriendCardResponse;
import com.qnectdk.domain.friend.dto.FriendResponse;
import com.qnectdk.domain.friend.dto.FriendSummary;
import com.qnectdk.domain.friend.dto.ReceivedFriendRequestResponse;
import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.entity.FriendshipStatus;
import com.qnectdk.domain.friend.repository.FriendshipRepository;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.service.PersonCardService;
import com.qnectdk.domain.reminder.service.ReminderService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.qnectdk.domain.notification.entity.NotificationType;
import com.qnectdk.domain.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final PersonCardService personCardService;
    private final PointService pointService;
    private final ReminderService reminderService;
    private final NotificationService notificationService;

    @Transactional
    public FriendResponse request(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        boolean exists =
                friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId)
                        || friendshipRepository.existsByRequesterIdAndAddresseeId(addresseeId, requesterId);

        if (exists) {
            throw new BusinessException(ErrorCode.ALREADY_FRIENDS);
        }

        Friendship saved = friendshipRepository.save(Friendship.request(requesterId, addresseeId));
        return FriendResponse.from(saved);
    }

    @Transactional
    public FriendResponse accept(Long friendshipId, Long currentUserId) {
        Friendship f = getOrThrow(friendshipId);
        validateAddressee(f, currentUserId);

        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException(ErrorCode.FRIENDSHIP_NOT_PENDING);
        }

        f.accept();

        Long requesterId = f.getRequesterId();
        Long addresseeId = f.getAddresseeId();

        // 친구 추가 알림 — 요청 보낸 사람(requester)에게 "수락됐어요"
        // refId = 친구가 된 상대(addressee = 수락한 사람)의 userId
        notificationService.push(
                requesterId,
                NotificationType.FRIEND_ADD,
                "새로운 친구가 1명 추가되었습니다!",
                "친구 요청이 수락되었어요.",
                addresseeId
        );

        // 리마인드 예약 — 양쪽 다
        reminderService.scheduleReminder(f.getId(), requesterId, f.getAcceptedAt());
        reminderService.scheduleReminder(f.getId(), addresseeId, f.getAcceptedAt());

        // 마일스톤 — 양쪽 다
        pointService.earnFriendMilestone(requesterId, getFriendIds(requesterId).size());
        pointService.earnFriendMilestone(addresseeId, getFriendIds(addresseeId).size());

        return FriendResponse.from(f);
    }

    @Transactional
    public FriendResponse reject(Long friendshipId, Long currentUserId) {
        Friendship f = getOrThrow(friendshipId);
        validateAddressee(f, currentUserId);

        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException(ErrorCode.FRIENDSHIP_NOT_PENDING);
        }

        f.reject();
        return FriendResponse.from(f);
    }

    public List<FriendResponse> getFriends(Long userId) {
        return friendshipRepository
                .findAcceptedFriendsOf(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(FriendResponse::from)
                .toList();
    }

    /** 받은 친구 요청 목록을 요청자(requester)의 person 카드와 함께 반환. */
    public List<ReceivedFriendRequestResponse> getReceivedRequests(Long userId) {
        List<Friendship> requests = friendshipRepository
                .findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING);
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requesterIds = requests.stream().map(Friendship::getRequesterId).toList();
        Map<Long, PersonCard> cardByUserId = personCardService.getCards(userId, requesterIds).stream()
                .collect(Collectors.toMap(PersonCard::userId, c -> c));
        return requests.stream()
                .map(f -> new ReceivedFriendRequestResponse(
                        f.getId(), f.getStatus(), f.getCreatedAt(), cardByUserId.get(f.getRequesterId())))
                .toList();
    }

    /** 내 친구 목록을 person 카드와 함께 반환. sort: "name"=이름 가나다순, 그 외(기본)=savedAt 최신순. */
    public List<FriendCardResponse> getFriendCards(Long userId, String sort) {
        List<Friendship> friendships = friendshipRepository
                .findAcceptedFriendsOf(userId, FriendshipStatus.ACCEPTED);
        if (friendships.isEmpty()) {
            return List.of();
        }

        List<Long> friendIds = friendships.stream()
                .map(f -> counterpartOf(f, userId))
                .toList();
        Map<Long, PersonCard> cardByUserId = personCardService.getCards(userId, friendIds).stream()
                .collect(Collectors.toMap(PersonCard::userId, c -> c));

        List<FriendCardResponse> cards = friendships.stream()
                .map(f -> {
                    LocalDateTime savedAt = f.getAcceptedAt() != null ? f.getAcceptedAt() : f.getCreatedAt();
                    return new FriendCardResponse(
                            f.getId(), savedAt, cardByUserId.get(counterpartOf(f, userId)));
                })
                .toList();

        Comparator<FriendCardResponse> comparator = "name".equalsIgnoreCase(sort)
                ? Comparator.comparing(c -> c.person() == null ? "" : c.person().name())
                : Comparator.comparing(FriendCardResponse::savedAt).reversed();
        return cards.stream().sorted(comparator).toList();
    }

    public List<Long> getFriendIds(Long userId) {
        return friendshipRepository
                .findAcceptedFriendsOf(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> f.getRequesterId().equals(userId)
                        ? f.getAddresseeId()
                        : f.getRequesterId())
                .toList();
    }

    public List<FriendSummary> getFriendSummaries(Long userId) {
        List<Long> friendIds = getFriendIds(userId);

        if (friendIds.isEmpty()) {
            return List.of();
        }

        return personCardService.getCards(userId, friendIds).stream()
                .map(c -> new FriendSummary(c.userId(), c.name(), c.characterId()))
                .toList();
    }

    private Friendship getOrThrow(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIENDSHIP_NOT_FOUND));
    }

    private void validateAddressee(Friendship f, Long currentUserId) {
        if (!f.getAddresseeId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_FRIENDSHIP_ADDRESSEE);
        }
    }

    private Long counterpartOf(Friendship f, Long userId) {
        return f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
    }
}