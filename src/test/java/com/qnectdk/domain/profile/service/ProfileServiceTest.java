package com.qnectdk.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qnectdk.domain.interest.service.InterestService;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProfileService 단위 테스트 (Mockito).
 *
 * <p>검증 범위:
 * <ul>
 *   <li>프로필 최초 생성 시 PROFILE_CREATE 적립 1회(인자 검증) — Requirements 1.1</li>
 *   <li>기존 프로필 갱신 시 적립 0회 — Requirements 1.2</li>
 *   <li>earn 예외 시 upsert가 예외 전파(롤백 의도) — Requirements 1.5</li>
 *   <li>getCharacters 17건 반환 — Requirements 5.2</li>
 *   <li>setCharacterImage 유효/무효/프로필 부재 분기 — Requirements 6.2, 6.4, 6.6</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    private static final String SHARE_BASE_URL = "https://qnect.example.com/p";

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private PointService pointService;
    @Mock
    private InterestService interestService;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                profileRepository, userQueryService, pointService, SHARE_BASE_URL, interestService);
    }

    private ProfileRequest sampleRequest() {
        return new ProfileRequest("국민대학교", Gender.MALE, "ENFP", "소주 3잔", "치킨");
    }

    private UserSummary sampleUser(Long userId) {
        return new UserSummary(userId, "홍길동", LocalDate.of(2000, 1, 1), "ABCD1234");
    }

    @Test
    @DisplayName("신규 프로필 생성 시 PROFILE_CREATE 적립을 정확한 인자로 1회 호출한다")
    void upsert_whenProfileAbsent_earnsPointOnce() {
        Long userId = 1L;
        when(userQueryService.getById(userId)).thenReturn(sampleUser(userId));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        profileService.upsert(userId, sampleRequest());

        verify(pointService, times(1))
                .earn(eq(userId), eq(PointPolicy.PROFILE_CREATE), eq(PointReason.PROFILE_CREATE), isNull());
    }

    @Test
    @DisplayName("기존 프로필 갱신 시 적립을 호출하지 않는다")
    void upsert_whenProfileExists_neverEarns() {
        Long userId = 2L;
        Profile existing = Profile.create(userId, "기존대학교", Gender.FEMALE, "INTJ", "맥주 2잔", "피자");
        when(userQueryService.getById(userId)).thenReturn(sampleUser(userId));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        profileService.upsert(userId, sampleRequest());

        verify(pointService, never()).earn(any(), org.mockito.ArgumentMatchers.anyInt(), any(), any());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("earn이 예외를 던지면 upsert가 예외를 전파한다(트랜잭션 롤백 의도)")
    void upsert_whenEarnThrows_propagates() {
        Long userId = 3L;
        when(userQueryService.getById(userId)).thenReturn(sampleUser(userId));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                .when(pointService)
                .earn(eq(userId), eq(PointPolicy.PROFILE_CREATE), eq(PointReason.PROFILE_CREATE), isNull());

        assertThatThrownBy(() -> profileService.upsert(userId, sampleRequest()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("getCharacters는 카탈로그 19건을 반환한다")
    void getCharacters_returns17() {
        List<CharacterResponse> characters = profileService.getCharacters();

        assertThat(characters).hasSize(19);
        assertThat(characters).allSatisfy(c -> assertThat(c.characterId()).isNotBlank());
    }

    @Test
    @DisplayName("유효한 캐릭터 식별자로 설정하면 characterId를 저장하고 반환한다")
    void setCharacterImage_validId_savesAndReturns() {
        Long userId = 4L;
        CharacterImage target = CharacterImage.CHARACTER_07;
        Profile profile = Profile.create(userId, "국민대학교", Gender.MALE, "ENFP", "소주 3잔", "치킨");
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        ImageResponse response = profileService.setCharacterImage(userId, target.getCharacterId());

        assertThat(response.characterId()).isEqualTo(target.getCharacterId());
        assertThat(profile.getCharacterId()).isEqualTo(target.getCharacterId());
    }

    @Test
    @DisplayName("카탈로그에 없는 식별자는 INVALID_INPUT(400)으로 거절한다")
    void setCharacterImage_invalidId_throwsInvalidInput() {
        assertThatThrownBy(() -> profileService.setCharacterImage(5L, "not-a-character"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);

        verify(profileRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("프로필이 없는 사용자가 캐릭터 설정을 요청하면 PROFILE_NOT_FOUND(404)")
    void setCharacterImage_profileAbsent_throwsProfileNotFound() {
        Long userId = 6L;
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.setCharacterImage(userId, CharacterImage.CHARACTER_01.getCharacterId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }
}
