package com.qnectdk.domain.interest.controller;

import com.qnectdk.domain.interest.dto.InterestCategoryResponse;
import com.qnectdk.domain.interest.dto.InterestResponse;
import com.qnectdk.domain.interest.dto.InterestUpdateRequest;
import com.qnectdk.domain.interest.service.InterestService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "관심사", description = "관심사 마스터 조회 및 내 관심사 설정 API")
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @Operation(summary = "전체 관심사 조회", description = "선택 가능한 전체 관심사를 카테고리별로 묶어 반환한다.")
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    )
    @GetMapping
    public ApiResponse<List<InterestCategoryResponse>> getAll() {
        return ApiResponse.ok(interestService.getAllGroupedByCategory());
    }

    @Operation(summary = "내 관심사 조회", description = "현재 로그인한 사용자가 설정한 관심사 목록을 반환한다.")
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    )
    @GetMapping("/me")
    public ApiResponse<List<InterestResponse>> getMine(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(interestService.getMine(user.getUserId()));
    }

    @Operation(summary = "내 관심사 전체 교체", description = "내 관심사를 요청 목록으로 전체 교체한다. 빈 배열은 전체 해제를 의미한다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "INTEREST_NOT_FOUND: 존재하지 않는 관심사 ID가 포함됨")
    })
    @PutMapping("/me")
    public ApiResponse<List<InterestResponse>> replaceMine(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody InterestUpdateRequest request) {
        return ApiResponse.ok(interestService.replaceMine(user.getUserId(), request.interestIds()));
    }
}
