package com.qnectdk.domain.user.controller;

import com.qnectdk.domain.user.dto.CheckLoginIdResponse;
import com.qnectdk.domain.user.dto.LoginRequest;
import com.qnectdk.domain.user.dto.LogoutRequest;
import com.qnectdk.domain.user.dto.RefreshRequest;
import com.qnectdk.domain.user.dto.SignupRequest;
import com.qnectdk.domain.user.dto.TokenResponse;
import com.qnectdk.domain.user.service.AuthService;
import com.qnectdk.global.response.ApiResponse;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @GetMapping("/check-login-id")
    public ApiResponse<CheckLoginIdResponse> checkLoginId(@RequestParam String loginId) {
        return ApiResponse.ok(new CheckLoginIdResponse(authService.isLoginIdAvailable(loginId)));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.ok();
    }
}
