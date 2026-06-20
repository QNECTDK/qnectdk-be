package com.qnectdk.domain.profile.dto;

import com.qnectdk.domain.profile.entity.Gender;
import com.qnectdk.domain.profile.entity.Profile;
import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.global.util.AgeUtil;
import com.qnectdk.global.util.ZodiacUtil;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 프로필 응답. 나이·띠는 저장하지 않고 사용자 생년월일로 계산해 채운다.
 * 프로필 미작성 시 {@link #ofUserOnly}로 기본정보(name/age/zodiac)만 채우고
 * 프로필 필드는 null, profileCompleted=false 로 내려준다.
 */
public record ProfileResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "나이(생년월일로 계산)", example = "23")
        int age,
        @Schema(description = "띠(생년월일로 계산)", example = "토끼")
        String zodiac,
        @Schema(description = "학교명", example = "국민대학교")
        String school,
        @Schema(description = "성별")
        Gender gender,
        @Schema(description = "MBTI", example = "ENFP")
        String mbti,
        @Schema(description = "주량", example = "소주 3잔")
        String drinkLevel,
        @Schema(description = "좋아하는 음식", example = "치킨")
        String favoriteFood,
        @Schema(description = "프로필 이미지 URL", example = "http://localhost:8080/files/3f2a1b9c4e.jpg")
        String imageUrl,
        @Schema(description = "QR/공유용 고유 코드", example = "Ab3xYz9Q")
        String publicCode,
        @Schema(description = "온보딩 완료 여부(프로필 작성됨)", example = "true")
        boolean profileCompleted
) {

    public static ProfileResponse of(Profile profile, UserSummary user) {
        return new ProfileResponse(
                user.userId(),
                user.name(),
                AgeUtil.of(user.birthDate()),
                ZodiacUtil.of(user.birthDate()),
                profile.getSchool(),
                profile.getGender(),
                profile.getMbti(),
                profile.getDrinkLevel(),
                profile.getFavoriteFood(),
                profile.getCharacterId(),
                user.publicCode(),
                true
        );
    }

    public static ProfileResponse ofUserOnly(UserSummary user) {
        return new ProfileResponse(
                user.userId(),
                user.name(),
                AgeUtil.of(user.birthDate()),
                ZodiacUtil.of(user.birthDate()),
                null, null, null, null, null,
                null,
                user.publicCode(),
                false
        );
    }
}
