package com.qnectdk.domain.profile.controller;

import com.qnectdk.domain.profile.dto.CharacterResponse;
import com.qnectdk.domain.profile.dto.ImageResponse;
import com.qnectdk.domain.profile.dto.ProfileImageRequest;
import com.qnectdk.domain.profile.dto.ProfileRequest;
import com.qnectdk.domain.profile.dto.ProfileResponse;
import com.qnectdk.domain.profile.dto.ShareResponse;
import com.qnectdk.domain.profile.service.ProfileService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "프로필", description = "프로필 조회·작성·공유 API")
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "내 프로필 조회", description = "내 프로필을 반환한다. 미작성이면 기본정보만 채우고 profileCompleted=false 로 내려준다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"))
    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMine(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(profileService.getMine(user.getUserId()));
    }

    // 온보딩·수정 공용 업서트: 없으면 생성, 있으면 교체
    @Operation(summary = "프로필 작성·수정", description = "프로필을 생성하거나 전체 교체한다. 온보딩·수정 공용.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패")
    })
    @PutMapping("/me")
    public ApiResponse<ProfileResponse> upsert(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok(profileService.upsert(user.getUserId(), request));
    }

    @Operation(summary = "캐릭터 카탈로그 조회", description = "프로필 이미지로 선택 가능한 캐릭터 17종을 반환한다.")
    @GetMapping("/characters")
    public ApiResponse<List<CharacterResponse>> getCharacters(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(profileService.getCharacters());
    }

    @Operation(summary = "프로필 이미지(캐릭터) 설정", description = "카탈로그의 캐릭터 식별자로 프로필 이미지를 설정한다. 무효 식별자는 400, 프로필 없으면 404.")
    @PutMapping("/me/image")
    public ApiResponse<ImageResponse> setImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ProfileImageRequest request) {
        return ApiResponse.ok(profileService.setCharacterImage(user.getUserId(), request.characterId()));
    }

    @Operation(summary = "공유 정보 조회", description = "QR/링크 공유에 필요한 publicCode 와 공유 URL 을 반환한다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"))
    @GetMapping("/me/share")
    public ApiResponse<ShareResponse> getShareInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(profileService.getShareInfo(user.getUserId()));
    }

    @Operation(summary = "공개코드로 프로필 조회", description = "publicCode 로 다른 사용자의 프로필을 조회한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 publicCode의 사용자를 찾을 수 없음(USER_NOT_FOUND)")
    })
    @GetMapping("/{publicCode}")
    public ApiResponse<ProfileResponse> getByPublicCode(
            @Parameter(description = "QR/공유용 고유 코드", example = "Ab3xYz9Q") @PathVariable String publicCode) {
        return ApiResponse.ok(profileService.getByPublicCode(publicCode));
    }
}
