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

    @Transactional
    public ProfileResponse create(Long userId, ProfileRequest request) {
        if (profileRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }
        UserSummary user = userQueryService.getById(userId);
        Profile profile = profileRepository.save(Profile.create(
                userId, request.school(), request.gender(),
                request.mbti(), request.drinkLevel(), request.favoriteFood()));
        return ProfileResponse.of(profile, user);
    }

    public ProfileResponse getMine(Long userId) {
        Profile profile = getByUserIdOrThrow(userId);
        return ProfileResponse.of(profile, userQueryService.getById(userId));
    }

    @Transactional
    public ProfileResponse update(Long userId, ProfileRequest request) {
        Profile profile = getByUserIdOrThrow(userId);
        profile.updateBasicInfo(request.school(), request.gender(),
                request.mbti(), request.drinkLevel(), request.favoriteFood());
        return ProfileResponse.of(profile, userQueryService.getById(userId));
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
        Profile profile = getByUserIdOrThrow(user.userId());
        return ProfileResponse.of(profile, user);
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
