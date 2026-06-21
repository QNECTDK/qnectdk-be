package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.quiz.entity.QuizAttempt;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 친구 퀴즈 목록 한 줄. 친구(person)별 활성 퀴즈 유무와 내 최근 응시 결과를 담는다.
 * 화면은 person(캐릭터+이름) + 총문항(빈 동그라미) + 내 정답수(채운 동그라미) + 버튼(미응시/완료)을 그린다.
 */
public record FriendQuizResponse(
        @Schema(description = "친구 person 카드(캐릭터·이름 등)")
        PersonCard person,
        @Schema(description = "이 친구의 활성 퀴즈 존재 여부", example = "true")
        boolean hasQuiz,
        @Schema(description = "활성 퀴즈 ID(없으면 null)", example = "12")
        Long quizId,
        @Schema(description = "활성 퀴즈의 총 문항 수(없으면 0)", example = "4")
        int totalQuestions,
        @Schema(description = "내가 이 퀴즈를 응시한 적이 있는지", example = "true")
        boolean attempted,
        @Schema(description = "내 최근 응시 정답 수(미응시면 null)", example = "3")
        Integer score
) {

    /** 활성 퀴즈가 없는 친구. */
    public static FriendQuizResponse noQuiz(PersonCard person) {
        return new FriendQuizResponse(person, false, null, 0, false, null);
    }

    /** 활성 퀴즈가 있는 친구. attempt 가 null 이면 미응시. */
    public static FriendQuizResponse of(PersonCard person, Long quizId, int totalQuestions, QuizAttempt attempt) {
        boolean attempted = attempt != null;
        Integer score = attempted ? attempt.getScore() : null;
        return new FriendQuizResponse(person, true, quizId, totalQuestions, attempted, score);
    }
}
