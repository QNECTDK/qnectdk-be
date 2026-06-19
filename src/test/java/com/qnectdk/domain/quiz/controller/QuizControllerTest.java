package com.qnectdk.domain.quiz.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qnectdk.domain.quiz.service.QuizService;
import com.qnectdk.global.config.SecurityConfig;
import com.qnectdk.global.security.CustomUserDetails;
import com.qnectdk.global.security.JwtAccessDeniedHandler;
import com.qnectdk.global.security.JwtAuthenticationEntryPoint;
import com.qnectdk.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * QuizController 슬라이스 테스트(@WebMvcTest).
 * 실제 SecurityConfig(JWT 필터 체인)를 로드해 인증/미인증 동작을 그대로 검증한다.
 * JwtTokenProvider만 모킹하고, 401 응답을 만드는 EntryPoint/AccessDeniedHandler는 실제 빈을 사용한다.
 */
@WebMvcTest(QuizController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("DELETE /api/quizzes/me - 인증 사용자면 deleteMyQuiz 호출 후 200 OK 반환")
    void deleteMine_authenticated_callsServiceAndReturnsOk() throws Exception {
        Long userId = 42L;
        Authentication auth = authenticationFor(userId);

        mockMvc.perform(delete("/api/quizzes/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(quizService).deleteMyQuiz(userId);
    }

    @Test
    @DisplayName("DELETE /api/quizzes/me - 미인증 요청이면 401 반환하고 서비스 호출 안 함")
    void deleteMine_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/quizzes/me"))
                .andExpect(status().isUnauthorized());

        verify(quizService, never()).deleteMyQuiz(anyLong());
    }

    private Authentication authenticationFor(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId);
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }
}
