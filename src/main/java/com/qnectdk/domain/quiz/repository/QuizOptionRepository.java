package com.qnectdk.domain.quiz.repository;

import com.qnectdk.domain.quiz.entity.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {

    List<QuizOption> findByQuestionIdOrderBySeqAsc(Long questionId);

    List<QuizOption> findByQuestionIdInOrderBySeqAsc(List<Long> questionIds);

    // 퀴즈 콘텐츠 교체 시 보기 일괄 삭제(단건 DELETE N+1 방지). 문항보다 먼저 호출해야 한다.
    @Modifying
    @Query("delete from QuizOption o where o.questionId in (select q.id from QuizQuestion q where q.quizId = :quizId)")
    void deleteByQuizId(@Param("quizId") Long quizId);
}
