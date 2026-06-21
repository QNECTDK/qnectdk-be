package com.qnectdk.domain.quiz.dto;

import com.qnectdk.domain.profile.dto.PersonCard;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 홈 "이 사람을 기억하나요?" 리마인드 카드. 오늘 복습 대상 친구 1명과, 풀 수 있는 그 친구의 활성 퀴즈를 담는다.
 * 대상이 없으면 컨트롤러가 data=null 로 응답한다.
 */
public record ReminderCardResponse(
        @Schema(description = "복습 대상 친구 person 카드(캐릭터·이름·학교 등)")
        PersonCard person,
        @Schema(description = "이 친구의 활성 퀴즈 존재 여부", example = "true")
        boolean hasQuiz,
        @Schema(description = "이 친구의 활성 퀴즈 ID(없으면 null) — 풀기 진입용", example = "12")
        Long quizId
) {
    public static ReminderCardResponse of(PersonCard person, Long quizId) {
        return new ReminderCardResponse(person, quizId != null, quizId);
    }
}
