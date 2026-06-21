package com.qnectdk.domain.quiz.port;

import com.qnectdk.domain.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * friend 도메인(B) 경계 어댑터. 기존 {@link FriendService#getFriendIds} 로 ACCEPTED 친구 userId 를 얻는다.
 * 엔티티가 아닌 service 만 사용해 경계를 지킨다.
 *
 * <p>친구 ID 목록 조회 전용 시그니처는 B와 합의 필요 — B가 전용 메서드/빈을 제공하면 이 어댑터를 교체한다. // B 합의 필요
 */
@Component
@RequiredArgsConstructor
public class FriendQueryAdapter implements FriendQueryPort {

    private final FriendService friendService;

    @Override
    public List<Long> findFriendIds(Long userId) {
        return friendService.getFriendIds(userId);
    }
}
