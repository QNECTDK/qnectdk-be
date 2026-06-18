package com.qnectdk.domain.point.service;

import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.entity.PointTransaction;
import com.qnectdk.domain.point.repository.PointTransactionRepository;
import com.qnectdk.domain.user.entity.User;
import com.qnectdk.domain.user.repository.UserRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointTransactionRepository txRepository;
    private final UserRepository userRepository;

    /**
     * 포인트 적립/차감 (문서의 earn). 내역 추가 + 잔액 갱신을 한 트랜잭션으로 묶음.
     * amount: 적립 +, 차감 -
     * 멱등성: 같은 (userId, reason, refId)가 이미 있으면 중복 적립 안 함.
     */
    @Transactional
    public void earn(Long userId, int amount, PointReason reason, Long refId) {
        // 멱등성 체크 (refId가 있는 1회성 적립만 — 출석처럼 refId 없는 건 호출부에서 날짜로 제어)
        if (refId != null
                && txRepository.existsByUserIdAndReasonAndRefId(userId, reason, refId)) {
            return; // 이미 지급됨 — 조용히 무시
        }

        // 1) 잔액 갱신 (User의 addPoint 호출)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.addPoint(amount);

        // 2) 거래 내역 한 줄 쌓기 (잔액 갱신 직후 값 기록)
        txRepository.save(
                PointTransaction.of(userId, amount, reason, refId, user.getPointBalance())
        );
        // @Transactional이라 1)2)가 한 묶음으로 커밋됨
    }

    /**
     * 친구 수 마일스톤 포인트. 정확히 5/15/30명일 때만 1회 지급.
     * friendCount: 수락 직후의 현재 친구 수
     */
    @Transactional
    public void earnFriendMilestone(Long userId, int friendCount) {
        int amount = switch (friendCount) {
            case 5  -> PointPolicy.FRIEND_MILESTONE_5;
            case 15 -> PointPolicy.FRIEND_MILESTONE_15;
            case 30 -> PointPolicy.FRIEND_MILESTONE_30;
            default -> 0; // 그 외 인원수는 지급 안 함
        };
        if (amount == 0) {
            return;
        }
        // refId = 달성 인원수 (5/15/30) → 같은 단계 중복 지급 방지(멱등)
        earn(userId, amount, PointReason.FRIEND_MILESTONE, (long) friendCount);
    }

    /**
     * 포인트 차감 (상점 구매 등). 잔액 부족 시 예외.
     * cost: 차감할 양 (양수로 전달)
     */
    @Transactional
    public void spend(Long userId, int cost, PointReason reason, Long refId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getPointBalance() < cost) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT); // 잔액 부족
        }

        user.addPoint(-cost); // 차감 (음수)
        txRepository.save(
                PointTransaction.of(userId, -cost, reason, refId, user.getPointBalance())
        );
    }

    // 현재 잔액 조회 (users.point_balance 캐시를 읽음 — 빠름)
    public int getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getPointBalance();
    }
}

