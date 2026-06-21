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
import com.qnectdk.domain.interest.service.InterestService;
import com.qnectdk.domain.profile.dto.PersonInfo;
import java.util.Map;
import java.util.stream.Collectors;
import com.qnectdk.global.util.ZodiacCharacterUtil;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserQueryService userQueryService;
    private final PointService pointService;
    private final String shareBaseUrl;
    private final InterestService interestService;

    public ProfileService(ProfileRepository profileRepository,
                          UserQueryService userQueryService,
                          PointService pointService,
                          @Value("${app.share.base-url}") String shareBaseUrl,
                          InterestService interestService) {
        this.profileRepository = profileRepository;
        this.userQueryService = userQueryService;
        this.pointService = pointService;
        this.shareBaseUrl = shareBaseUrl;
        this.interestService = interestService;
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

    /** 프로필 이미지로 선택 가능한 캐릭터 카탈로그(19종)를 조회한다. */
    public List<CharacterResponse> getCharacters() {
        return CharacterImage.all();
    }

    /** 카탈로그의 캐릭터 식별자로 프로필 이미지를 설정한다. 무효 식별자는 400, 프로필 부재는 404. */
    @Transactional
    public ImageResponse setCharacterImage(Long userId, String characterId) {
        CharacterImage character = CharacterImage.findById(characterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        Profile profile = getByUserIdOrThrow(userId);
        profile.updateCharacterId(character.getCharacterId());
        return new ImageResponse(character.getCharacterId());
      }

      /**
       * 상점에서 장착한 캐릭터를 프로필에 반영한다(shop → profile 연동).
       * characterId=null 이면 띠 기본 캐릭터로 되돌린다. 프로필 미작성이면 아무것도 하지 않는다.
       */
      @Transactional
      public void applyCharacter(Long userId, String characterId) {
        profileRepository.findByUserId(userId)
            .ifPresent(profile -> profile.updateCharacterId(characterId));
    }

    public ProfileResponse getByPublicCode(String publicCode) {
        UserSummary user = userQueryService.getByPublicCode(publicCode);
        return profileRepository.findByUserId(user.userId())
                .map(profile -> ProfileResponse.of(profile, user))
                .orElseGet(() -> ProfileResponse.ofUserOnly(user));
    }

    /**
     * userId 로 다른 사용자의 프로필을 조회한다(친구 목록 → 상세 진입용).
     * 친구가 아니어도 조회는 허용한다(공개 프로필 성격). 미작성 프로필은 기본정보만 채워 내려준다.
     */
    public ProfileResponse getByUserId(Long userId) {
      UserSummary user = userQueryService.getById(userId);
      return profileRepository.findByUserId(userId)
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

    /**
     * 여러 userId의 person 정보를 한 번에 조회 (친구·그룹 카드 조립용).
     * user(name) + profile(school/gender/mbti/characterId) + interest(이름들)를 합친다.
     * 프로필 미작성 사용자는 profile 필드가 null로 채워진다.
     */
    public List<PersonInfo> getPersonsByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        // 1) user 정보 (name, birthDate) — A의 UserQueryService
        Map<Long, UserSummary> userMap = userQueryService.getByIds(userIds).stream()
                .collect(Collectors.toMap(UserSummary::userId, u -> u));
        // 2) profile 정보 (school, gender, mbti, characterId)
        Map<Long, Profile> profileMap = profileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));
        // 3) interest 이름들
        Map<Long, List<String>> interestMap = interestService.getNamesByUserIds(userIds);

        // 4) 합치기
        return userIds.stream()
                .map(uid -> {
                    UserSummary user = userMap.get(uid);
                    if (user == null) return null; // 없는 사용자 스킵
                    Profile profile = profileMap.get(uid);
                    return new PersonInfo(
                            uid,
                            user.name(),
                            ZodiacCharacterUtil.resolve(
                                    profile != null ? profile.getCharacterId() : null,
                                    user.birthDate()),
                            profile != null ? profile.getSchool() : null,
                            profile != null && profile.getGender() != null ? profile.getGender().name() : null,
                            user.birthDate().getYear(),
                            profile != null ? profile.getMbti() : null,
                            interestMap.getOrDefault(uid, List.of())
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

}
