package com.qnectdk.domain.friend.service;

import com.qnectdk.domain.friend.dto.FriendResponse;
import com.qnectdk.domain.friend.dto.FriendSummary;
import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.entity.FriendshipStatus;
import com.qnectdk.domain.friend.repository.FriendshipRepository;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.user.service.UserQueryService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserQueryService userQueryService;
    private final PointService pointService;

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

        // 친구 성사 → 양쪽 모두 친구 수 +1 → 각자 마일스톤 체크
        Long requesterId = f.getRequesterId();
        Long addresseeId = f.getAddresseeId();
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
                .stream().map(FriendResponse::from).toList();
    }

    public List<FriendResponse> getReceivedRequests(Long userId) {
        return friendshipRepository
                .findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream().map(FriendResponse::from).toList();
    }

    public List<Long> getFriendIds(Long userId) {
        return friendshipRepository
                .findAcceptedFriendsOf(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId())
                .toList();
    }

    public List<FriendSummary> getFriendSummaries(Long userId) {
        List<Long> friendIds = getFriendIds(userId);
        if (friendIds.isEmpty()) {
            return List.of();
        }
        return userQueryService.getByIds(friendIds).stream()
                .map(u -> new FriendSummary(u.userId(), u.name()))
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
}