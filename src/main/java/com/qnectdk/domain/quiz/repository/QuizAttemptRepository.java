package com.qnectdk.domain.quiz.repository;

import com.qnectdk.domain.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // 이 solver 가 이 owner 의 퀴즈(어떤 세트든)를 푼 적이 있는지. "친구 1명당 첫 풀기 1회" 포인트 판정용.
    @Query("""
            select count(a) > 0 from QuizAttempt a, Quiz q
            where a.quizId = q.id
              and a.solverId = :solverId
              and q.ownerId = :ownerId
            """)
    boolean existsBySolverForOwner(@Param("solverId") Long solverId, @Param("ownerId") Long ownerId);

    // solver 가 주어진 퀴즈들에 대해 남긴 응시 기록(최신순). 친구 퀴즈 목록에서 퀴즈별 최근 점수 산출용.
    // 호출부가 quizId 별 첫 항목(=최신)만 취하면 퀴즈별 최근 응시가 된다.
    @Query("""
        select a from QuizAttempt a
        where a.solverId = :solverId
          and a.quizId in :quizIds
        order by a.id desc
        """)
    List<QuizAttempt> findBySolverAndQuizIdsOrderByIdDesc(
        @Param("solverId") Long solverId, @Param("quizIds") Collection<Long> quizIds);
}
