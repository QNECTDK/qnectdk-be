package com.qnectdk.domain.daily.port;

import com.qnectdk.domain.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * friend 도메인(B) 경계 어댑터. {@link FriendService#getFriendIds} 로 내가 저장한 친구 userId
 * 목록을 얻는다.
 * 엔티티가 아닌 service 만 사용해 경계를 지킨다.
 */
@Component("dailyFriendQueryAdapter")
@RequiredArgsConstructor
public class FriendQueryAdapter implements FriendQueryPort {

    private final FriendService friendService;

    @Override
    public List<Long> findFriendIds(Long userId) {
      return friendService.getFriendIds(userId);
    }
}
