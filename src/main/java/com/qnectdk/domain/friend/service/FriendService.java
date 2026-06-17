package com.qnectdk.domain.friend.service;

import com.qnectdk.domain.friend.dto.FriendResponse;
import com.qnectdk.domain.friend.entity.Friendship;
import com.qnectdk.domain.friend.entity.FriendshipStatus;
import com.qnectdk.domain.friend.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendshipRepository friendshipRepository;

    // 친구 추가 요청
    @Transactional
    public FriendResponse request(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청할 수 없습니다.");
        }
        // 양방향 중복 체크 (내가 보냈거나, 상대가 이미 나에게 보냈거나)
        boolean exists =
                friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId)
                        || friendshipRepository.existsByRequesterIdAndAddresseeId(addresseeId, requesterId);
        if (exists) {
            throw new IllegalArgumentException("이미 친구이거나 요청이 존재합니다.");
        }

        Friendship saved = friendshipRepository.save(
                Friendship.request(requesterId, addresseeId)
        );
        return FriendResponse.from(saved);
    }

    // 친구 요청 수락 (요청을 받은 사람 = addressee만 가능)
    @Transactional
    public FriendResponse accept(Long friendshipId, Long currentUserId) {
        Friendship f = getOrThrow(friendshipId);
        validateAddressee(f, currentUserId);
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 수락할 수 있습니다.");
        }
        f.accept();
        return FriendResponse.from(f);
    }

    // 친구 요청 거절 (addressee만 가능)
    @Transactional
    public FriendResponse reject(Long friendshipId, Long currentUserId) {
        Friendship f = getOrThrow(friendshipId);
        validateAddressee(f, currentUserId);
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 거절할 수 있습니다.");
        }
        f.reject();
        return FriendResponse.from(f);
    }

    // 내 친구 목록 (ACCEPTED, 양쪽 합산)
    public List<FriendResponse> getFriends(Long userId) {
        return friendshipRepository
                .findAcceptedFriendsOf(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(FriendResponse::from)
                .toList();
    }

    // 내가 받은 친구 요청 목록 (PENDING)
    public List<FriendResponse> getReceivedRequests(Long userId) {
        return friendshipRepository
                .findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(FriendResponse::from)
                .toList();
    }

    // --- private helpers ---

    private Friendship getOrThrow(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다. id=" + friendshipId));
    }

    private void validateAddressee(Friendship f, Long currentUserId) {
        if (!f.getAddresseeId().equals(currentUserId)) {
            throw new IllegalArgumentException("요청을 받은 사람만 수락/거절할 수 있습니다.");
        }
    }
}