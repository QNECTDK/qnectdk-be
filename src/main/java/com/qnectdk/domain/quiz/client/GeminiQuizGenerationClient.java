package com.qnectdk.domain.quiz.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.qnectdk.domain.quiz.client.dto.GeneratedQuiz;
import com.qnectdk.domain.quiz.client.dto.QuizGenerationCommand;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Gemini 기반 퀴즈 생성기. app.gemini.enabled=true 일 때만 활성화된다.
 *
 * <p>API 키는 서버 env(GEMINI_API_KEY)에서만 주입하며, URL 쿼리가 아닌 헤더(x-goog-api-key)로 전달해
 * 로그/에러 메시지에 노출되지 않게 한다. JSON 스키마 강제 + 파싱 검증 + 재시도/타임아웃을 적용한다.
 */
@Component
@ConditionalOnProperty(name = "app.gemini.enabled", havingValue = "true")
public class GeminiQuizGenerationClient implements QuizGenerationClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiQuizGenerationClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxRetries;

    public GeminiQuizGenerationClient(
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.gemini.model:gemini-3.1-flash-lite}") String model,
            @Value("${app.gemini.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.gemini.read-timeout-ms:20000}") int readTimeoutMs,
            @Value("${app.gemini.max-retries:2}") int maxRetries) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "app.gemini.enabled=true 이면 GEMINI_API_KEY(app.gemini.api-key)가 필요합니다.");
        }
        // LLM 이 enum 을 소문자/혼합 케이스로 반환해도 파싱되도록 관용 매퍼를 전용으로 둔다(GeneratedQuiz 는 단순 타입만 포함).
        this.objectMapper = JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        this.maxRetries = Math.max(0, maxRetries);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public GeneratedQuiz generate(QuizGenerationCommand command) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", buildPrompt(command))))),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.9));
        String path = "/models/" + model + ":generateContent";

        int attempts = maxRetries + 1;
        for (int i = 1; i <= attempts; i++) {
            try {
                String response = restClient.post()
                        .uri(path)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
                return parse(response);
            } catch (Exception e) {
                // 예외 메시지는 응답 본문/URL 등이 섞일 수 있어 클래스명만 남긴다(키 유출 방지).
                log.warn("Gemini 퀴즈 생성 실패 ({}/{}): {}", i, attempts, e.getClass().getSimpleName());
            }
        }
        throw new BusinessException(ErrorCode.QUIZ_GENERATION_FAILED);
    }

    private GeneratedQuiz parse(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        String text = root.path("candidates").path(0)
                .path("content").path("parts").path(0).path("text").asText("");
        if (text.isBlank()) {
            throw new IllegalStateException("Gemini 응답 본문이 비어 있습니다.");
        }
        GeneratedQuiz quiz = objectMapper.readValue(text, GeneratedQuiz.class);
        if (quiz.questions() == null || quiz.questions().isEmpty()) {
            throw new IllegalStateException("생성된 문항이 없습니다.");
        }
        return quiz;
    }

    private String buildPrompt(QuizGenerationCommand c) {
        return """
                너는 친구 사이의 아이스브레이킹 퀴즈를 만드는 도우미야.
                아래 사람의 프로필을 바탕으로, 이 사람에 관한 %d~%d개의 퀴즈 문항을 만들어줘.
                규칙:
                - 각 문항 type 은 "MULTIPLE"(4지선다) 또는 "OX" 중 하나.
                - MULTIPLE 은 options 를 4개 주고 그중 정확히 1개만 correct=true, correctAnswer 는 그 정답 보기 텍스트와 동일하게.
                - OX 는 options 를 빈 배열로 두고 correctAnswer 는 "O" 또는 "X".
                - 최소 한 문항은 required=true(꼭 맞춰야 하는 핵심 문항).
                반드시 아래 JSON 스키마로만, 다른 설명 없이 출력해:
                {"questions":[{"type":"MULTIPLE","content":"...","correctAnswer":"...","required":true,"options":[{"content":"...","correct":true}]}]}

                프로필:
                - 이름: %s
                - 학교: %s
                - 성별: %s
                - MBTI: %s
                - 주량: %s
                - 좋아하는 음식: %s
                - 관심사: %s
                """.formatted(
                c.minQuestions(), c.maxQuestions(),
                nz(c.ownerName()), nz(c.school()), nz(c.gender()), nz(c.mbti()),
                nz(c.drinkLevel()), nz(c.favoriteFood()), String.join(", ", c.interests()));
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
