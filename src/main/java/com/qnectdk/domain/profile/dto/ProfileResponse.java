package com.qnectdk.domain.profile.dto;

import com.qnectdk.domain.profile.entity.Gender;
import com.qnectdk.domain.profile.entity.Profile;
import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.global.util.AgeUtil;
import com.qnectdk.global.util.ZodiacUtil;

/**
 * 프로필 응답. 나이·띠는 저장하지 않고 사용자 생년월일로 계산해 채운다.
 */
public record ProfileResponse(
        Long userId,
        String name,
        int age,
        String zodiac,
        String school,
        Gender gender,
        String mbti,
        String drinkLevel,
        String favoriteFood,
        String imageUrl,
        String publicCode
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
                profile.getImageUrl(),
                user.publicCode()
        );
    }
}
