package com.qnectdk.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.point.entity.PointPolicy;
import com.qnectdk.domain.point.entity.PointReason;
import com.qnectdk.domain.point.service.PointService;
import com.qnectdk.domain.profile.dto.CharacterResponse;
import com.qnectdk.domain.profile.dto.ImageResponse;
import com.qnectdk.domain.profile.dto.ProfileRequest;
import com.qnectdk.domain.profile.entity.CharacterImage;
import com.qnectdk.domain.profile.entity.Gender;
import com.qnectdk.domain.profile.entity.Profile;
import com.qnectdk.domain.profile.repository.ProfileRepository;
import com.qnectdk.domain.user.dto.UserSummary;
import com.qnectdk.domain.user.service.UserQueryService;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;

/**
 * ProfileService 속성 기반 테스트(jqwik). 각 속성 최소 100회 반복.
 * B 서비스(PointService)·리포지토리는 mock으로 대체한다.
 */
class ProfileServicePropertyTest {

    private static final String SHARE_BASE_URL = "https://qnect.example.com/p";

    private ProfileService newService(ProfileRepository profileRepository,
                                      UserQueryService userQueryService,
                                      PointService pointService) {
        return new ProfileService(profileRepository, userQueryService, pointService, SHARE_BASE_URL);
    }

    private ProfileRequest sampleRequest() {
        return new ProfileRequest("국민대학교", Gender.MALE, "ENFP", "소주 3잔", "치킨");
    }

    private UserSummary sampleUser(Long userId) {
        return new UserSummary(userId, "홍길동", LocalDate.of(2000, 1, 1), "ABCD1234");
    }

    // Feature: point-notification-integration, Property 1: 프로필 최초 생성 1회 적립
    // 프로필 부재 상태에서 upsert를 N회(N>=1) 호출하면 첫 호출(신규 생성)만 earn 1회,
    // 이후 갱신 호출은 0회 → 총 정확히 1회.
    // Validates: Requirements 1.1, 1.2, 1.3
    @Property(tries = 100)
    void profileCreate_earnsExactlyOnce_acrossRepeatedUpserts(
            @ForAll @LongRange(min = 1, max = 1_000_000) long userId,
            @ForAll @IntRange(min = 1, max = 12) int callCount) {

        ProfileRepository profileRepository = mock(ProfileRepository.class);
        UserQueryService userQueryService = mock(UserQueryService.class);
        PointService pointService = mock(PointService.class);
        ProfileService service = newService(profileRepository, userQueryService, pointService);

        Profile saved = Profile.create(userId, "국민대학교", Gender.MALE, "ENFP", "소주 3잔", "치킨");
        when(userQueryService.getById(userId)).thenReturn(sampleUser(userId));
        // 첫 조회는 부재(empty), 이후 모든 조회는 저장된 프로필을 반환(이미 존재).
        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.empty(), Optional.of(saved));
        when(profileRepository.save(any(Profile.class))).thenReturn(saved);

        for (int i = 0; i < callCount; i++) {
            service.upsert(userId, sampleRequest());
        }

        // 첫 호출에서만 적립 → 총 정확히 1회.
        verify(pointService, times(1))
                .earn(eq(userId), eq(PointPolicy.PROFILE_CREATE), eq(PointReason.PROFILE_CREATE), isNull());
    }

    // Feature: point-notification-integration, Property 8: 캐릭터 카탈로그 고정성
    // 임의의 카탈로그 조회 호출에 대해 항상 정확히 17건, characterId 집합은 항상 동일,
    // 각 항목의 characterId/imageUrl은 비어있지 않다.
    // Validates: Requirements 5.2, 5.3, 5.4
    @Property(tries = 100)
    void characterCatalog_isFixed_acrossRepeatedCalls(@ForAll @IntRange(min = 1, max = 5) int repeat) {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        UserQueryService userQueryService = mock(UserQueryService.class);
        PointService pointService = mock(PointService.class);
        ProfileService service = newService(profileRepository, userQueryService, pointService);

        Set<String> expectedIds = Arrays.stream(CharacterImage.values())
                .map(CharacterImage::getCharacterId)
                .collect(Collectors.toSet());

        for (int i = 0; i < repeat; i++) {
            List<CharacterResponse> catalog = service.getCharacters();

            assertThat(catalog).hasSize(17);
            assertThat(catalog).allSatisfy(c -> {
                assertThat(c.characterId()).isNotBlank();
                assertThat(c.imageUrl()).isNotBlank();
            });
            Set<String> ids = catalog.stream()
                    .map(CharacterResponse::characterId)
                    .collect(Collectors.toSet());
            assertThat(ids).isEqualTo(expectedIds);
        }
    }

    // Feature: point-notification-integration, Property 9: 캐릭터 선택 설정의 유효성 분기
    // 카탈로그에 있는 식별자면 updateImageUrl 호출 + 해당 imageUrl 반환,
    // 없으면 BusinessException(INVALID_INPUT) + updateImageUrl 미호출.
    // Validates: Requirements 6.2, 6.3, 6.4
    @Property(tries = 100)
    void setCharacterImage_branchesOnCatalogValidity(
            @ForAll @LongRange(min = 1, max = 1_000_000) long userId,
            @ForAll("identifiers") String characterId) {

        ProfileRepository profileRepository = mock(ProfileRepository.class);
        UserQueryService userQueryService = mock(UserQueryService.class);
        PointService pointService = mock(PointService.class);
        ProfileService service = newService(profileRepository, userQueryService, pointService);

        Optional<CharacterImage> match = CharacterImage.findById(characterId);

        if (match.isPresent()) {
            String expectedUrl = match.get().getImageUrl();
            Profile profile = mock(Profile.class);
            when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

            ImageResponse response = service.setCharacterImage(userId, characterId);

            assertThat(response.imageUrl()).isEqualTo(expectedUrl);
            verify(profile, times(1)).updateImageUrl(expectedUrl);
        } else {
            assertThatThrownBy(() -> service.setCharacterImage(userId, characterId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_INPUT);

            // 무효 식별자는 검증 단계에서 거절되므로 프로필 조회/이미지 변경이 일어나지 않는다.
            verify(profileRepository, never()).findByUserId(any());
        }
    }

    /** 카탈로그의 유효 식별자와 카탈로그에 없는 임의 문자열을 섞어 생성한다. */
    @Provide
    Arbitrary<String> identifiers() {
        Set<String> validIds = Arrays.stream(CharacterImage.values())
                .map(CharacterImage::getCharacterId)
                .collect(Collectors.toSet());

        Arbitrary<String> valid = Arbitraries.of(validIds.toArray(new String[0]));
        Arbitrary<String> invalid = Arbitraries.strings()
                .ofMinLength(0)
                .ofMaxLength(20)
                .filter(s -> !validIds.contains(s));

        return Arbitraries.oneOf(valid, invalid);
    }
}
