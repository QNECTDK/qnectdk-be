package com.qnectdk.domain.quiz.port;

import java.util.List;

/**
 * 친구 ID 목록 조회 경계 포트(친구 퀴즈 목록용). friend 도메인(B)이 소유한다. // B 합의 필요
 * 전용 시그니처는 B와 합의 필요 — 현재는 {@link FriendQueryAdapter} 가 기존 FriendService 로 임시 구현한다.
 *
 * <p>참고: 직접 FriendService 를 주입하지 않고 포트로 분리한 이유는 도메인 경계(A→B는 인터페이스만)
 * 준수와 함께, {@code QuizService ← ReminderService} 의존과 엮인 순환 참조를 피하기 위함이다.
 */
public interface FriendQueryPort {

    /** 주어진 사용자의 ACCEPTED 친구 userId 목록. */
    List<Long> findFriendIds(Long userId);
}
