package com.qnectdk.domain.quiz.port;

/**
 * 포인트 적립 경계 포트. 실제 구현은 B 의 point 도메인이 소유한다. // B 합의 필요
 * A(quiz)는 친구가 첫 퀴즈 풀기를 완료했을 때 이 포트로 적립을 요청한다(친구 1명당 1회).
 *
 * <p><b>멱등성은 B 의 책임이다.</b> A 의 호출부는 best-effort 게이트만 두므로(동시 응시 시 경합 가능),
 * B 는 원장(point_transactions)에서 (userId=solverId, reason=QUIZ_FIRST_SOLVE, ref=ownerId) 단위로
 * 중복 적립을 막아야 한다. 그래서 친구 식별을 위해 ownerId 를 함께 넘긴다. // B 합의 필요
 *
 * <p>B 가 이 인터페이스를 구현한 빈을 제공하면 {@link PointPortStubConfig} 의 무동작 스텁이
 * {@code @ConditionalOnMissingBean} 으로 자동 대체된다.
 */
public interface PointPort {

    /**
     * 첫 퀴즈 풀기 완료 적립(point_reason = QUIZ_FIRST_SOLVE). B 가 친구(ownerId) 단위로 1회만 적립해야 한다.
     *
     * @param solverId 퀴즈를 푼 사용자
     * @param ownerId  퀴즈 주인(친구) — 1명당 1회 멱등 키
     * @param quizId   푼 퀴즈 세트 id (적립 근거 ref)
     */
    void earnQuizFirstSolve(Long solverId, Long ownerId, Long quizId);
}
