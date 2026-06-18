package com.qnectdk.domain.quiz.client;

import com.qnectdk.domain.quiz.client.dto.GeneratedQuiz;
import com.qnectdk.domain.quiz.client.dto.GeneratedQuiz.GeneratedOption;
import com.qnectdk.domain.quiz.client.dto.GeneratedQuiz.GeneratedQuestion;
import com.qnectdk.domain.quiz.client.dto.QuizGenerationCommand;
import com.qnectdk.domain.quiz.entity.QuestionType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * API 키 없이 동작하는 기본 퀴즈 생성기(개발/시연/테스트용).
 * app.gemini.enabled 가 false 이거나 미설정이면 활성화된다. 항상 유효한(검증 통과) 퀴즈를 만든다.
 */
@Component
@ConditionalOnProperty(name = "app.gemini.enabled", havingValue = "false", matchIfMissing = true)
public class MockQuizGenerationClient implements QuizGenerationClient {

    @Override
    public GeneratedQuiz generate(QuizGenerationCommand c) {
        String name = orDefault(c.ownerName(), "이 친구");
        List<GeneratedQuestion> questions = new ArrayList<>();

        questions.add(multiple(
                name + "의 MBTI는?",
                orDefault(c.mbti(), "ENFP"),
                List.of("ISTJ", "ENFP", "INFJ", "ESTP"),
                true));

        questions.add(multiple(
                name + "이(가) 다니는 학교는?",
                orDefault(c.school(), "비밀"),
                List.of(orDefault(c.school(), "비밀"), "서울대", "연세대", "고려대"),
                false));

        questions.add(ox(
                name + "은(는) 술을 잘 마신다 (주량: " + orDefault(c.drinkLevel(), "비공개") + ")",
                "O"));

        questions.add(multiple(
                name + "이(가) 좋아하는 음식은?",
                orDefault(c.favoriteFood(), "치킨"),
                List.of(orDefault(c.favoriteFood(), "치킨"), "피자", "초밥", "떡볶이"),
                false));

        if (!c.interests().isEmpty()) {
            questions.add(multiple(
                    name + "의 관심사 중 하나는?",
                    c.interests().get(0),
                    List.of(c.interests().get(0), "여행", "운동", "독서"),
                    false));
        }

        int limit = Math.min(questions.size(), Math.max(c.minQuestions(), Math.min(questions.size(), c.maxQuestions())));
        return new GeneratedQuiz(List.copyOf(questions.subList(0, limit)));
    }

    private static final int MAX_OPTIONS = 4;

    private GeneratedQuestion multiple(String content, String correct, List<String> rawOptions, boolean required) {
        LinkedHashSet<String> texts = new LinkedHashSet<>();
        texts.add(correct); // 정답을 먼저 넣어 캡(4개) 후에도 항상 포함되게 한다.
        texts.addAll(rawOptions);
        while (texts.size() < 2) {
            texts.add("기타" + texts.size());
        }
        List<GeneratedOption> options = texts.stream()
                .limit(MAX_OPTIONS)
                .map(text -> new GeneratedOption(text, text.equals(correct)))
                .toList();
        return new GeneratedQuestion(QuestionType.MULTIPLE, content, correct, required, options);
    }

    private GeneratedQuestion ox(String content, String correctAnswer) {
        return new GeneratedQuestion(QuestionType.OX, content, correctAnswer, false, List.of());
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
