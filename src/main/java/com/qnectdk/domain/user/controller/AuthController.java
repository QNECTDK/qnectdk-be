package com.qnectdk.domain.user.controller;

import com.qnectdk.domain.user.dto.CheckLoginIdResponse;
import com.qnectdk.domain.user.dto.LoginRequest;
import com.qnectdk.domain.user.dto.LogoutRequest;
import com.qnectdk.domain.user.dto.RefreshRequest;
import com.qnectdk.domain.user.dto.SignupRequest;
import com.qnectdk.domain.user.dto.TokenResponse;
import com.qnectdk.domain.user.service.AuthService;
import com.qnectdk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 갱신/로그아웃 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "신규 회원을 등록하고 가입 즉시 액세스/리프레시 토큰을 발급한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "이미 사용 중인 아이디(DUPLICATE_LOGIN_ID) 또는 전화번호(DUPLICATE_PHONE)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "입력값 검증 실패")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @Operation(summary = "아이디 중복확인", description = "주어진 로그인 아이디의 사용 가능 여부를 확인한다.")
    @GetMapping("/check-login-id")
    public ApiResponse<CheckLoginIdResponse> checkLoginId(
            @Parameter(description = "중복 확인할 로그인 아이디", example = "tester01")
            @RequestParam String loginId) {
        return ApiResponse.ok(new CheckLoginIdResponse(authService.isLoginIdAvailable(loginId)));
    }

    @Operation(summary = "로그인", description = "아이디/비밀번호로 인증하고 토큰을 발급한다.")
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "아이디 또는 비밀번호 불일치(INVALID_CREDENTIALS)")
    )
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 토큰을 발급한다(리프레시 토큰 회전).")
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "유효하지 않거나 만료된 리프레시 토큰")
    )
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화한다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.ok();
    }
}
