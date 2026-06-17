package com.qnectdk.domain.friend.service;

import com.qnectdk.domain.friend.dto.FriendMemoResponse;
import com.qnectdk.domain.friend.entity.FriendMemo;
import com.qnectdk.domain.friend.repository.FriendMemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendMemoService {

    private final FriendMemoRepository friendMemoRepository;

    // 메모 작성 or 수정 (owner+friend 당 1개라 있으면 수정, 없으면 생성)
    @Transactional
    public FriendMemoResponse upsert(Long ownerId, Long friendId, String content) {
        FriendMemo memo = friendMemoRepository
                .findByOwnerIdAndFriendId(ownerId, friendId)
                .map(existing -> {
                    existing.updateContent(content);
                    return existing;
                })
                .orElseGet(() -> friendMemoRepository.save(
                        FriendMemo.create(ownerId, friendId, content)
                ));
        return FriendMemoResponse.from(memo);
    }

    // 특정 친구에 대한 내 메모 조회
    public FriendMemoResponse get(Long ownerId, Long friendId) {
        FriendMemo memo = friendMemoRepository
                .findByOwnerIdAndFriendId(ownerId, friendId)
                .orElseThrow(() -> new IllegalArgumentException("메모가 없습니다."));
        return FriendMemoResponse.from(memo);
    }
}