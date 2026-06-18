package com.qnectdk.domain.daily.port;

import java.util.List;

/**
 * 친구 ID 목록 조회 경계 포트(데일리 친구 통계용). friend 도메인(B)이 소유한다. // B 합의 필요
 * 전용 시그니처는 B와 합의 필요 — 현재는 {@link FriendQueryAdapter} 가 기존 FriendService 로 임시 구현한다.
 */
public interface FriendQueryPort {

    /** 주어진 사용자의 ACCEPTED 친구 userId 목록. */
    List<Long> findFriendIds(Long userId);
}
