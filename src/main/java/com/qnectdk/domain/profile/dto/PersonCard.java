package com.qnectdk.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PersonCard(
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
        List<String> interests,
        @Schema(description = "조회자 기준 이 사람이 속한 내 그룹 이름들", example = "[\"밴드부\"]")
        List<String> groupTags
) {
    /** PersonInfo + groupTags → 완성된 person */
    public static PersonCard of(PersonInfo info, List<String> groupTags) {
        return new PersonCard(
                info.userId(), info.name(), info.characterId(),
                info.school(), info.gender(), info.birthYear(),
                info.mbti(), info.interests(),
                groupTags
        );
    }
}