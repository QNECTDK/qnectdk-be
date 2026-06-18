package com.qnectdk.domain.quiz.service;

import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizOption;
import com.qnectdk.domain.quiz.entity.QuizQuestion;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.repository.QuizOptionRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import com.qnectdk.domain.quiz.service.QuizDraft.OptionDraft;
import com.qnectdk.domain.quiz.service.QuizDraft.QuestionDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 퀴즈 쓰기 작업(원자적). 외부 호출(Gemini) 없이 DB 변경만 담당한다 —
 * 덕분에 생성 오케스트레이션({@code QuizService.generateQuiz})은 느린 외부 호출을 트랜잭션 밖에서 하고,
 * 검증을 통과한 결과만 이 빈의 트랜잭션으로 원자적으로 저장한다.
 * 입력 draft 는 호출 전에 {@link QuizContentValidator} 로 검증돼 있어야 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizWriter {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;

    /** 새 active 퀴즈 생성(기존 active 비활성화) + 콘텐츠 저장. 자동 생성/리마인드 경로. */
    public Quiz persistNewQuiz(Long ownerId, QuizType type, QuizDraft draft) {
        deactivateActive(ownerId);
        Quiz quiz = quizRepository.save(Quiz.create(ownerId, type));
        insertContent(quiz.getId(), draft);
        return quiz;
    }

    /** active 퀴즈 콘텐츠 전체 교체(없으면 FIRST_MEET 로 생성). 수동 편집·저장 경로. */
    public Quiz replaceActiveContent(Long ownerId, QuizDraft draft) {
        Quiz quiz = quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId)
                .orElseGet(() -> quizRepository.save(Quiz.create(ownerId, QuizType.FIRST_MEET)));
        deleteContent(quiz.getId());
        insertContent(quiz.getId(), draft);
        return quiz;
    }

    private void deactivateActive(Long ownerId) {
        quizRepository.findByOwnerIdAndActiveTrue(ownerId).forEach(Quiz::deactivate);
    }

    private void deleteContent(Long quizId) {
        // 보기 → 문항 순서로 벌크 삭제(단건 DELETE N+1 방지). 이후 insertContent 의 INSERT 는 삭제 뒤 실행된다.
        optionRepository.deleteByQuizId(quizId);
        questionRepository.deleteByQuizId(quizId);
    }

    private void insertContent(Long quizId, QuizDraft draft) {
        int questionSeq = 0;
        for (QuestionDraft questionDraft : draft.questions()) {
            QuizQuestion question = questionRepository.save(QuizQuestion.create(
                    quizId,
                    questionDraft.type(),
                    questionDraft.content(),
                    questionDraft.correctAnswer(),
                    questionDraft.required(),
                    questionSeq++));
            if (questionDraft.type() == QuestionType.OX || questionDraft.options() == null) {
                continue;
            }
            int optionSeq = 0;
            for (OptionDraft optionDraft : questionDraft.options()) {
                optionRepository.save(QuizOption.create(
                        question.getId(), optionDraft.content(), optionDraft.correct(), optionSeq++));
            }
        }
    }
}
