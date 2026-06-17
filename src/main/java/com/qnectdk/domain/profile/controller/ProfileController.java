package com.qnectdk.domain.profile.controller;

import com.qnectdk.domain.profile.dto.ImageResponse;
import com.qnectdk.domain.profile.dto.ProfileRequest;
import com.qnectdk.domain.profile.dto.ProfileResponse;
import com.qnectdk.domain.profile.dto.ShareResponse;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfileResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok(profileService.create(user.getUserId(), request));
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMine(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(profileService.getMine(user.getUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<ProfileResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok(profileService.update(user.getUserId(), request));
    }

    @PostMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageResponse> uploadImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart("image") MultipartFile image) {
        return ApiResponse.ok(profileService.updateImage(user.getUserId(), image));
    }

    @GetMapping("/me/share")
    public ApiResponse<ShareResponse> getShareInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(profileService.getShareInfo(user.getUserId()));
    }

    @GetMapping("/{publicCode}")
    public ApiResponse<ProfileResponse> getByPublicCode(@PathVariable String publicCode) {
        return ApiResponse.ok(profileService.getByPublicCode(publicCode));
    }
}
