package com.qnectdk.domain.quiz.service;

import com.qnectdk.domain.interest.dto.InterestResponse;
import com.qnectdk.domain.interest.service.InterestService;
import com.qnectdk.domain.profile.dto.ProfileResponse;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.domain.quiz.client.QuizGenerationClient;
import com.qnectdk.domain.quiz.client.dto.GeneratedQuiz;
import com.qnectdk.domain.quiz.client.dto.QuizGenerationCommand;
import com.qnectdk.domain.quiz.dto.AiQuizDraftResponse;
import com.qnectdk.domain.quiz.dto.QuizAttemptRequest;
import com.qnectdk.domain.quiz.dto.QuizResponse;
import com.qnectdk.domain.quiz.dto.QuizResultResponse;
import com.qnectdk.domain.quiz.dto.QuizSaveRequest;
import com.qnectdk.domain.quiz.dto.SolvableQuizResponse;
import com.qnectdk.domain.quiz.entity.QuestionType;
import com.qnectdk.domain.quiz.entity.Quiz;
import com.qnectdk.domain.quiz.entity.QuizAnswer;
import com.qnectdk.domain.quiz.entity.QuizAttempt;
import com.qnectdk.domain.quiz.entity.QuizOption;
import com.qnectdk.domain.quiz.entity.QuizQuestion;
import com.qnectdk.domain.quiz.entity.QuizType;
import com.qnectdk.domain.quiz.port.PointPort;
import com.qnectdk.domain.quiz.repository.QuizAnswerRepository;
import com.qnectdk.domain.quiz.repository.QuizAttemptRepository;
import com.qnectdk.domain.quiz.repository.QuizOptionRepository;
import com.qnectdk.domain.quiz.repository.QuizQuestionRepository;
import com.qnectdk.domain.quiz.repository.QuizRepository;
import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.domain.user.service.UserQueryService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 퀴즈 도메인 오케스트레이션. 타 도메인은 service 호출로만 접근하고(엔티티 import 금지),
 * 쓰기는 {@link QuizWriter} 에 위임한다.
 *
 * <p>트랜잭션 경계: 읽기는 클래스 기본 readOnly, {@code solve} 는 읽기+쓰기 단일 트랜잭션,
 * 외부 호출이 있는 {@code generateQuiz}/저장 경로는 NOT_SUPPORTED 로 트랜잭션 밖에서 수행 후
 * QuizWriter 가 원자적으로 저장한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAnswerRepository answerRepository;
    private final QuizWriter quizWriter;
    private final QuizContentValidator validator;
    private final QuizGenerationClient generationClient;
    private final PointPort pointPort;
    private final UserQueryService userQueryService;
    private final ProfileService profileService;
    private final InterestService interestService;

    /**
     * 현재 active 퀴즈(본인 편집 뷰). B 의 리마인드 스케줄러도 호출. // B 합의 필요
     */
    public QuizResponse getActiveQuiz(Long ownerId) {
        return assemble(getActiveQuizOrThrow(ownerId));
    }

    /**
     * AI 문제 초안 생성. 프로필 기반으로 문제 여러 개를 만들어 반환하되 <b>저장하지 않는다</b> —
     * 클라이언트가 "내 퀴즈 목록"에 추가·취사선택한 뒤 {@link #saveMyQuiz}(PUT)로 저장한다.
     * 서버는 기존 퀴즈를 덮어쓰지 않는다(완전 additive). 외부 호출이 있어 트랜잭션 밖에서 수행한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AiQuizDraftResponse generateDraft(Long ownerId, QuizType type, int count) {
        int n = Math.max(1, Math.min(count, QuizContentValidator.MAX_QUESTIONS));
        QuizGenerationCommand command = buildGenerationCommand(ownerId, type, n, n);
        GeneratedQuiz generated = generationClient.generate(command);
        QuizDraft draft = toDraft(generated);
        validator.validateEach(draft);
        return AiQuizDraftResponse.from(draft);
    }

    /**
     * 리마인드 퀴즈 재생성(D+30). B 의 리마인드 스케줄러가 호출 — 서버가 직접 생성·저장(교체)한다. // B 합의 필요
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public QuizResponse generateReminderQuiz(Long ownerId) {
        QuizGenerationCommand command = buildGenerationCommand(
                ownerId, QuizType.REMIND, QuizContentValidator.MIN_QUESTIONS, QuizContentValidator.MAX_QUESTIONS);
        GeneratedQuiz generated = generationClient.generate(command);
        QuizDraft draft = toDraft(generated);
        validator.validate(draft);
        Quiz quiz = quizWriter.persistNewQuiz(ownerId, QuizType.REMIND, draft);
        return assemble(quiz.getId());
    }

    /**
     * 퀴즈 편집·저장(본인). 문항/보기 직접 작성·수정·추가. 외부 호출이 없으므로 단일 읽기+쓰기 트랜잭션으로 처리한다.
     */
    @Transactional
    public QuizResponse saveMyQuiz(Long ownerId, QuizSaveRequest request) {
        QuizDraft draft = toDraft(request);
        validator.validate(draft);
        Quiz quiz = quizWriter.replaceActiveContent(ownerId, draft);
        return assemble(quiz.getId());
    }

    /**
     * 친구 퀴즈 조회(푸는 화면). 정답 미노출. solvable=false 면 프로필 미완성 → 열람만 가능.
     */
    public SolvableQuizResponse getSolvableQuiz(Long ownerId, Long viewerId) {
        if (ownerId.equals(viewerId)) {
            throw new BusinessException(ErrorCode.QUIZ_FORBIDDEN);
        }
        Quiz quiz = getActiveQuizOrThrow(ownerId);
        String ownerName = userQueryService.getById(ownerId).name();
        boolean solvable = isProfileCompleted(viewerId);
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderBySeqAsc(quiz.getId());
        return SolvableQuizResponse.of(quiz, ownerName, solvable, questions, loadOptions(questions));
    }

    /**
     * 응시·채점. 응시자 프로필이 완성돼야 풀 수 있고, 이 친구의 퀴즈를 처음 푼 경우에만 포인트를 적립한다.
     */
    @Transactional
    public QuizResultResponse solve(Long ownerId, Long solverId, QuizAttemptRequest request) {
        if (ownerId.equals(solverId)) {
            throw new BusinessException(ErrorCode.QUIZ_FORBIDDEN);
        }
        if (!isProfileCompleted(solverId)) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_SOLVABLE);
        }
        Quiz quiz = getActiveQuizOrThrow(ownerId);
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderBySeqAsc(quiz.getId());
        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND);
        }

        Map<Long, String> submitted = request.answers().stream()
                .collect(Collectors.toMap(
                        QuizAttemptRequest.AnswerInput::questionId,
                        QuizAttemptRequest.AnswerInput::answer,
                        (first, ignored) -> first));
        boolean firstSolve = !attemptRepository.existsBySolverForOwner(solverId, ownerId);

        List<Graded> graded = new ArrayList<>();
        int score = 0;
        for (QuizQuestion question : questions) {
            String answer = submitted.getOrDefault(question.getId(), "");
            boolean correct = isCorrect(question, answer);
            if (correct) {
                score++;
            }
            graded.add(new Graded(question, answer, correct));
        }

        QuizAttempt attempt = attemptRepository.save(
                QuizAttempt.create(quiz.getId(), solverId, score, questions.size()));

        List<QuizAnswer> answerEntities = new ArrayList<>();
        List<QuizResultResponse.AnswerResult> results = new ArrayList<>();
        for (Graded g : graded) {
            answerEntities.add(QuizAnswer.create(attempt.getId(), g.question().getId(), g.answer(), g.correct()));
            results.add(new QuizResultResponse.AnswerResult(
                    g.question().getId(), g.question().getContent(), g.answer(),
                    g.question().getCorrectAnswer(), g.correct()));
        }
        answerRepository.saveAll(answerEntities);

        // firstSolve 는 best-effort 게이트(동시 응시 시 경합 가능) — 친구 1명당 1회 멱등성은 B 의 포인트 원장이 보장한다.
        // 재응시는 ERD(quiz_attempts 비유니크 인덱스)상 허용. // B 합의 필요
        if (firstSolve) {
            pointPort.earnQuizFirstSolve(solverId, ownerId, quiz.getId());
        }
        return new QuizResultResponse(attempt.getId(), quiz.getId(), score, questions.size(), results);
    }

    /**
     * 응시 결과 상세(문항별 정/오답). 본인 응시 기록만 조회 가능.
     */
    public QuizResultResponse getResult(Long attemptId, Long requesterId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND));
        if (!attempt.getSolverId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.QUIZ_FORBIDDEN);
        }
        Map<Long, QuizQuestion> questionsById = questionRepository.findByQuizIdOrderBySeqAsc(attempt.getQuizId())
                .stream()
                .collect(Collectors.toMap(QuizQuestion::getId, question -> question,
                        (first, ignored) -> first, LinkedHashMap::new));
        List<QuizResultResponse.AnswerResult> results = answerRepository.findByAttemptId(attemptId).stream()
                .map(answer -> {
                    QuizQuestion question = questionsById.get(answer.getQuestionId());
                    return new QuizResultResponse.AnswerResult(
                            answer.getQuestionId(),
                            question == null ? "" : question.getContent(),
                            answer.getAnswer(),
                            question == null ? "" : question.getCorrectAnswer(),
                            answer.isCorrect());
                })
                .toList();
        return new QuizResultResponse(
                attempt.getId(), attempt.getQuizId(), attempt.getScore(), attempt.getTotal(), results);
    }

    // --- helpers ---

    private boolean isCorrect(QuizQuestion question, String answer) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        String submitted = answer.trim();
        String correct = question.getCorrectAnswer().trim();
        // OX 는 대소문자 무시(o/x 허용), 객관식 보기 텍스트는 대소문자 의미를 보존한다.
        return question.getType() == QuestionType.OX
                ? submitted.equalsIgnoreCase(correct)
                : submitted.equals(correct);
    }

    private Quiz getActiveQuizOrThrow(Long ownerId) {
        return quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
    }

    private QuizResponse assemble(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        return assemble(quiz);
    }

    private QuizResponse assemble(Quiz quiz) {
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderBySeqAsc(quiz.getId());
        return QuizResponse.of(quiz, questions, loadOptions(questions));
    }

    private Map<Long, List<QuizOption>> loadOptions(List<QuizQuestion> questions) {
        List<Long> questionIds = questions.stream().map(QuizQuestion::getId).toList();
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return optionRepository.findByQuestionIdInOrderBySeqAsc(questionIds).stream()
                .collect(Collectors.groupingBy(QuizOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));
    }

    private boolean isProfileCompleted(Long userId) {
        return profileService.getMine(userId).profileCompleted();
    }

    private QuizGenerationCommand buildGenerationCommand(Long ownerId, QuizType type, int minQuestions, int maxQuestions) {
        UserSummary user = userQueryService.getById(ownerId);
        ProfileResponse profile = profileService.getMine(ownerId);
        List<String> interests = interestService.getMine(ownerId).stream()
                .map(InterestResponse::name)
                .toList();
        String gender = profile.gender() == null ? null : profile.gender().name();
        return new QuizGenerationCommand(
                user.name(), profile.school(), gender, profile.mbti(),
                profile.drinkLevel(), profile.favoriteFood(), interests,
                type, minQuestions, maxQuestions);
    }

    private QuizDraft toDraft(QuizSaveRequest request) {
        List<QuizDraft.QuestionDraft> questions = request.questions().stream()
                .map(question -> new QuizDraft.QuestionDraft(
                        question.type(), question.content(), question.correctAnswer(), question.required(),
                        question.options() == null ? List.of()
                                : question.options().stream()
                                .map(option -> new QuizDraft.OptionDraft(option.content(), option.correct()))
                                .toList()))
                .toList();
        return new QuizDraft(questions);
    }

    private QuizDraft toDraft(GeneratedQuiz generated) {
        List<QuizDraft.QuestionDraft> questions = generated.questions().stream()
                .map(question -> new QuizDraft.QuestionDraft(
                        question.type(), question.content(), question.correctAnswer(), question.required(),
                        question.options() == null ? List.of()
                                : question.options().stream()
                                .map(option -> new QuizDraft.OptionDraft(option.content(), option.correct()))
                                .toList()))
                .toList();
        return new QuizDraft(questions);
    }

    private record Graded(QuizQuestion question, String answer, boolean correct) {
    }
}
