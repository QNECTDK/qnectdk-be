package com.qnectdk.domain.quiz.repository;

import com.qnectdk.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizIdOrderBySeqAsc(Long quizId);

    // 여러 퀴즈의 문항 수를 한 번에 집계(친구 퀴즈 목록 batch, N+1 방지).
    @Query("select q.quizId as quizId, count(q) as count from QuizQuestion q where q.quizId in :quizIds group by q.quizId")
    List<QuizQuestionCount> countByQuizIds(@Param("quizIds") Collection<Long> quizIds);

    // 퀴즈 콘텐츠 교체 시 문항 일괄 삭제(단건 DELETE N+1 방지). 보기 삭제 이후에 호출한다.
    @Modifying
    @Query("delete from QuizQuestion q where q.quizId = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);

    // quizId 별 문항 수 집계 결과 projection.
    interface QuizQuestionCount {
      Long getQuizId();

      long getCount();
    }
}
