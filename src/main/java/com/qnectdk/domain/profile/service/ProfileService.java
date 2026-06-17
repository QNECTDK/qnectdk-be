package com.qnectdk.domain.profile.service;

import com.qnectdk.domain.profile.dto.ImageResponse;
import com.qnectdk.domain.profile.dto.ProfileRequest;
import com.qnectdk.domain.profile.dto.ProfileResponse;
import com.qnectdk.domain.profile.dto.ShareResponse;
import com.qnectdk.domain.profile.entity.Profile;
import com.qnectdk.domain.profile.repository.ProfileRepository;
import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.domain.user.service.UserQueryService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserQueryService userQueryService;
    private final StorageService storageService;
    private final String shareBaseUrl;

    public ProfileService(ProfileRepository profileRepository,
                          UserQueryService userQueryService,
                          StorageService storageService,
                          @Value("${app.share.base-url}") String shareBaseUrl) {
        this.profileRepository = profileRepository;
        this.userQueryService = userQueryService;
        this.storageService = storageService;
        this.shareBaseUrl = shareBaseUrl;
    }

    /**
     * 온보딩·수정 공용 업서트. 없으면 생성, 있으면 교체.
     * 첫 PUT 동시성 경합은 uk_profiles_user_id 유니크 제약이 막고,
     * GlobalExceptionHandler가 DataIntegrityViolation을 409로 변환한다.
     */
    @Transactional
    public ProfileResponse upsert(Long userId, ProfileRequest request) {
        UserSummary user = userQueryService.getById(userId);
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = profileRepository.save(Profile.create(
                    userId, request.school(), request.gender(),
                    request.mbti(), request.drinkLevel(), request.favoriteFood()));
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

    @Transactional
    public ImageResponse updateImage(Long userId, MultipartFile file) {
        Profile profile = getByUserIdOrThrow(userId);
        String previousImageUrl = profile.getImageUrl();
        String imageUrl = storageService.store(file);
        profile.updateImageUrl(imageUrl);
        storageService.delete(previousImageUrl); // 교체된 이전 파일 정리 (best-effort)
        return new ImageResponse(imageUrl);
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
