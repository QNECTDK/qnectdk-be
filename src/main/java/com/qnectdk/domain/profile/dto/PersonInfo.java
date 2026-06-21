package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PersonInfo(
        @Schema(description = "사용자 id", example = "34")
        Long userId,
        @Schema(description = "이름", example = "김철수")
        String name,
        @Schema(description = "캐릭터 식별자", example = "character07")
        String characterId,
        @Schema(description = "학교", example = "국민대학교")
        String school,
        @Schema(description = "성별", example = "MALE")
        String gender,
        @Schema(description = "출생연도", example = "2005")
        Integer birthYear,
        @Schema(description = "MBTI", example = "ISTJ")
        String mbti,
        @Schema(description = "관심사", example = "[\"여행\",\"음악감상\"]")
        List<String> interests
) {
}