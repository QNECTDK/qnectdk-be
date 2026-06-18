package com.qnectdk.domain.quiz.service;

import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.service.QuizDraft.OptionDraft;
import com.qnectdk.domain.quiz.service.QuizDraft.QuestionDraft;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 퀴즈 콘텐츠 불변식 검증. 수동 저장본과 Gemini 생성본 모두 영속화 전에 통과해야 한다.
 */
@Component
public class QuizContentValidator {

    public static final int MIN_QUESTIONS = 3;
    public static final int MAX_QUESTIONS = 5;
    // 객관식 보기 1~4개 가변 → 의미 있는 선택을 위해 최소 2, 디자인상 최대 4.
    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 4;
    private static final String OX_O = "O";
    private static final String OX_X = "X";

    /** 저장(PUT)용 — 문항 수(3~5) + 문항별 정합성 모두 검증. */
    public void validate(QuizDraft draft) {
        List<QuestionDraft> questions = draft.questions();
        if (questions == null || questions.size() < MIN_QUESTIONS || questions.size() > MAX_QUESTIONS) {
            throw invalid();
        }
        questions.forEach(this::validateQuestion);
    }

    /** AI 초안용 — 문항 수 제한 없이 문항별 정합성만 검증(초안은 클라이언트가 골라 담는다). */
    public void validateEach(QuizDraft draft) {
        if (draft.questions() == null) {
            throw invalid();
        }
        draft.questions().forEach(this::validateQuestion);
    }

    private void validateQuestion(QuestionDraft question) {
        if (question.type() == null || isBlank(question.content()) || isBlank(question.correctAnswer())) {
            throw invalid();
        }
        if (question.type() == QuestionType.OX) {
            validateOx(question);
            return;
        }
        validateMultiple(question);
    }

    private void validateOx(QuestionDraft question) {
        String answer = question.correctAnswer();
        if (!OX_O.equals(answer) && !OX_X.equals(answer)) {
            throw invalid();
        }
    }

    private void validateMultiple(QuestionDraft question) {
        List<OptionDraft> options = question.options();
        if (options == null || options.size() < MIN_OPTIONS || options.size() > MAX_OPTIONS) {
            throw invalid();
        }
        if (options.stream().anyMatch(option -> isBlank(option.content()))) {
            throw invalid();
        }
        long correctCount = options.stream().filter(OptionDraft::correct).count();
        if (correctCount != 1) {
            throw invalid();
        }
        boolean correctAnswerMatchesOption = options.stream()
                .anyMatch(option -> option.correct() && option.content().equals(question.correctAnswer()));
        if (!correctAnswerMatchesOption) {
            throw invalid();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BusinessException invalid() {
        return new BusinessException(ErrorCode.QUIZ_INVALID_CONTENT);
    }
}
