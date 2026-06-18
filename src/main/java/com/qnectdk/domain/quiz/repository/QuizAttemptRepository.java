package com.qnectdk.domain.quiz.repository;

import com.qnectdk.domain.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // 이 solver 가 이 owner 의 퀴즈(어떤 세트든)를 푼 적이 있는지. "친구 1명당 첫 풀기 1회" 포인트 판정용.
    @Query("""
            select count(a) > 0 from QuizAttempt a, Quiz q
            where a.quizId = q.id
              and a.solverId = :solverId
              and q.ownerId = :ownerId
            """)
    boolean existsBySolverForOwner(@Param("solverId") Long solverId, @Param("ownerId") Long ownerId);
}
