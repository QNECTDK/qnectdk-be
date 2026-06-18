package com.qnectdk.domain.quiz.repository;

import com.qnectdk.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // owner 의 현재 active 퀴즈(친구가 푸는 대상). 정상 상태에선 1개.
    Optional<Quiz> findFirstByOwnerIdAndActiveTrueOrderByIdDesc(Long ownerId);

    // 새 퀴즈 생성 시 기존 active 들을 모두 비활성화하기 위한 조회.
    List<Quiz> findByOwnerIdAndActiveTrue(Long ownerId);
}
