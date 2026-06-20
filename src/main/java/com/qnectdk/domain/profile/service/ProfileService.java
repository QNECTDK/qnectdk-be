package com.qnectdk.domain.profile.service;

import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.dto.CharacterResponse;
import com.qnectdk.domain.profile.dto.ImageResponse;
import com.qnectdk.domain.profile.dto.ProfileRequest;
import com.qnectdk.domain.profile.dto.ProfileResponse;
import com.qnectdk.domain.profile.dto.ShareResponse;
import com.qnectdk.domain.profile.entity.CharacterImage;
import com.qnectdk.domain.profile.entity.Profile;
import com.qnectdk.domain.profile.repository.ProfileRepository;
import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.domain.user.service.UserQueryService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserQueryService userQueryService;
    private final PointService pointService;
    private final String shareBaseUrl;

    public ProfileService(ProfileRepository profileRepository,
                          UserQueryService userQueryService,
                          PointService pointService,
                          @Value("${app.share.base-url}") String shareBaseUrl) {
        this.profileRepository = profileRepository;
        this.userQueryService = userQueryService;
        this.pointService = pointService;
        this.shareBaseUrl = shareBaseUrl;
    }

    /**
     * 온보딩·수정 공용 업서트. 없으면 생성, 있으면 교체.
     * 첫 PUT 동시성 경합은 uk_profiles_user_id 유니크 제약이 막고,
     * GlobalExceptionHandler가 DataIntegrityViolation을 409로 변환한다.
     * 신규 생성 분기에서만 프로필 최초 생성 포인트를 1회 적립한다.
     */
    @Transactional
    public ProfileResponse upsert(Long userId, ProfileRequest request) {
        UserSummary user = userQueryService.getById(userId);
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = profileRepository.save(Profile.create(
                    userId, request.school(), request.gender(),
                    request.mbti(), request.drinkLevel(), request.favoriteFood()));
            // 프로필 최초 생성 1회 적립 (refId=null → 최초 1회 판정은 호출부 책임)
            pointService.earn(userId, PointPolicy.PROFILE_CREATE, PointReason.PROFILE_CREATE, null);
        } else {
            profile.updateBasicInfo(request.school(), request.gender(),
                    request.mbti(), request.drinkLevel(), request.favoriteFood());
        }
        return ProfileResponse.of(profile, user);
    }

    public ProfileResponse getMine(Long userId) {
        UserSummary user = userQueryService.getById(userId);
        return profileRepository.findByUserId(userId)
                .map(profile -> ProfileResponse.of(profile, user))
                .orElseGet(() -> ProfileResponse.ofUserOnly(user));
    }

    /** 프로필 이미지로 선택 가능한 캐릭터 카탈로그(17종)를 조회한다. */
    public List<CharacterResponse> getCharacters() {
        return CharacterImage.all();
    }

    /** 카탈로그의 캐릭터 식별자로 프로필 이미지를 설정한다. 무효 식별자는 400, 프로필 부재는 404. */
    @Transactional
    public ImageResponse setCharacterImage(Long userId, String characterId) {
        CharacterImage character = CharacterImage.findById(characterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        Profile profile = getByUserIdOrThrow(userId);
        profile.updateImageUrl(character.getImageUrl());
        return new ImageResponse(character.getImageUrl());
    }

    public ProfileResponse getByPublicCode(String publicCode) {
        UserSummary user = userQueryService.getByPublicCode(publicCode);
        return profileRepository.findByUserId(user.userId())
                .map(profile -> ProfileResponse.of(profile, user))
                .orElseGet(() -> ProfileResponse.ofUserOnly(user));
    }

    public ShareResponse getShareInfo(Long userId) {
        UserSummary user = userQueryService.getById(userId);
        return new ShareResponse(user.publicCode(), buildShareUrl(user.publicCode()));
    }

    private String buildShareUrl(String publicCode) {
        return shareBaseUrl.endsWith("/") ? shareBaseUrl + publicCode : shareBaseUrl + "/" + publicCode;
    }

    private Profile getByUserIdOrThrow(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
