package com.qnectdk.domain.quiz.repository;

import com.qnectdk.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizIdOrderBySeqAsc(Long quizId);

    // 퀴즈 콘텐츠 교체 시 문항 일괄 삭제(단건 DELETE N+1 방지). 보기 삭제 이후에 호출한다.
    @Modifying
    @Query("delete from QuizQuestion q where q.quizId = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);
}
