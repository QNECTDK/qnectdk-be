package com.qnectdk.domain.friend.service;

import com.qnectdk.domain.friend.dto.FriendMemoResponse;
import com.qnectdk.domain.friend.entity.FriendMemo;
import com.qnectdk.domain.friend.repository.FriendMemoRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
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

    public FriendMemoResponse get(Long ownerId, Long friendId) {
        FriendMemo memo = friendMemoRepository
                .findByOwnerIdAndFriendId(ownerId, friendId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMO_NOT_FOUND));
        return FriendMemoResponse.from(memo);
    }
}