package com.qnectdk.domain.profile.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qnectdk.domain.profile.dto.ImageResponse;
import com.qnectdk.domain.profile.entity.CharacterImage;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.global.config.SecurityConfig;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import com.qnectdk.global.security.CustomUserDetails;
import com.qnectdk.global.security.JwtAccessDeniedHandler;
import com.qnectdk.global.security.JwtAuthenticationEntryPoint;
import com.qnectdk.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ProfileController 슬라이스 테스트.
 *
 * <p>프로덕션 {@link SecurityConfig}를 그대로 import 해 실제 보안 동작(CSRF 비활성·미인증 401 EntryPoint)을
 * 검증한다. 인증 사용자는 {@code CustomUserDetails} principal을 주입(@AuthenticationPrincipal 해석 경로 동일)한다.
 *
 * <p>검증 대상:
 * <ul>
 *   <li>GET /api/profiles/characters → 17건 반환·서비스 위임 (5.2, 5.5)</li>
 *   <li>PUT /api/profiles/me/image → @NotBlank 400(6.5)·무효 식별자 400(6.4)·정상 위임(6.7)</li>
 *   <li>미인증 요청 401 (5.6, 6.8)</li>
 * </ul>
 */
@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class ProfileControllerTest {

    private static final CustomUserDetails AUTH_USER = new CustomUserDetails(1L);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    // SecurityConfig 생성자 의존성(JWT 필터 구성). 슬라이스 테스트에서 실제 토큰 검증은 사용하지 않는다.
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /characters - 인증 사용자는 카탈로그 19종을 반환받고 서비스에 위임된다 (5.2/5.5)")
    void getCharacters_returnsSeventeen() throws Exception {
        when(profileService.getCharacters()).thenReturn(CharacterImage.all());

        mockMvc.perform(get("/api/profiles/characters").with(user(AUTH_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.length()", is(19)))
            .andExpect(jsonPath("$.data[0].characterId", is("character01")));

        verify(profileService).getCharacters();
    }

    @Test
    @DisplayName("GET /characters - 미인증 요청은 401 (5.6)")
    void getCharacters_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profiles/characters"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED.name())));

        verify(profileService, never()).getCharacters();
    }

    @Test
    @DisplayName("PUT /me/image - 유효 식별자는 characterId를 저장하고 서비스에 위임된다 (6.7)")
    void setImage_validIdentifier_delegatesAndReturnsImageUrl() throws Exception {
      String characterId = CharacterImage.CHARACTER_07.getCharacterId();
        when(profileService.setCharacterImage(eq(1L), eq("character07")))
            .thenReturn(new ImageResponse(characterId));

        mockMvc.perform(put("/api/profiles/me/image")
                        .with(user(AUTH_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"characterId\":\"character07\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.characterId", is(characterId)));

        verify(profileService).setCharacterImage(1L, "character07");
    }

    @Test
    @DisplayName("PUT /me/image - characterId 공백은 @NotBlank 위반으로 400, 서비스 미호출 (6.5)")
    void setImage_blankCharacterId_returns400() throws Exception {
        mockMvc.perform(put("/api/profiles/me/image")
                        .with(user(AUTH_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"characterId\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(ErrorCode.INVALID_INPUT.name())));

        verify(profileService, never()).setCharacterImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("PUT /me/image - characterId 빈 문자열은 @NotBlank 위반으로 400 (6.5)")
    void setImage_emptyCharacterId_returns400() throws Exception {
        mockMvc.perform(put("/api/profiles/me/image")
                        .with(user(AUTH_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"characterId\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is(ErrorCode.INVALID_INPUT.name())));

        verify(profileService, never()).setCharacterImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("PUT /me/image - 카탈로그에 없는 식별자는 서비스가 INVALID_INPUT을 던져 400 (6.4)")
    void setImage_invalidIdentifier_returns400() throws Exception {
        when(profileService.setCharacterImage(eq(1L), eq("character99")))
                .thenThrow(new BusinessException(ErrorCode.INVALID_INPUT));

        mockMvc.perform(put("/api/profiles/me/image")
                        .with(user(AUTH_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"characterId\":\"character99\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(ErrorCode.INVALID_INPUT.name())));

        verify(profileService).setCharacterImage(1L, "character99");
    }

    @Test
    @DisplayName("PUT /me/image - 미인증 요청은 401, 서비스 미호출 (6.8)")
    void setImage_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/profiles/me/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"characterId\":\"character07\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED.name())));

        verify(profileService, never()).setCharacterImage(anyLong(), any());
    }
}
