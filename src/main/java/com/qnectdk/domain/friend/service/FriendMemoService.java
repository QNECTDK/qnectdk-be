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

    @Transactional
    public FriendMemoResponse upsert(Long ownerId, Long friendId, String content) {
        FriendMemo memo = friendMemoRepository
                .findByOwnerIdAndFriendId(ownerId, friendId)
                .map(existing -> { existing.updateContent(content); return existing; })
                .orElseGet(() -> friendMemoRepository.save(FriendMemo.create(ownerId, friendId, content)));
        return FriendMemoResponse.from(memo);
    }

    /** 메모 조회. 아직 메모가 없으면 에러가 아니라 빈 메모(content=null)를 200으로 반환한다. */
    public FriendMemoResponse get(Long ownerId, Long friendId) {
      return friendMemoRepository
                .findByOwnerIdAndFriendId(ownerId, friendId)
            .map(FriendMemoResponse::from)
            .orElseGet(() -> FriendMemoResponse.empty(ownerId, friendId));
    }
}
