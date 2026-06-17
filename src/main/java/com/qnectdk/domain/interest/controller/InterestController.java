package com.qnectdk.domain.interest.controller;

import com.qnectdk.domain.interest.dto.InterestCategoryResponse;
import com.qnectdk.domain.interest.dto.InterestResponse;
import com.qnectdk.domain.interest.dto.InterestUpdateRequest;
import com.qnectdk.domain.interest.service.InterestService;
import com.qnectdk.global.response.ApiResponse;
import com.qnectdk.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @GetMapping
    public ApiResponse<List<InterestCategoryResponse>> getAll() {
        return ApiResponse.ok(interestService.getAllGroupedByCategory());
    }

    @GetMapping("/me")
    public ApiResponse<List<InterestResponse>> getMine(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(interestService.getMine(user.getUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<List<InterestResponse>> replaceMine(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody InterestUpdateRequest request) {
        return ApiResponse.ok(interestService.replaceMine(user.getUserId(), request.interestIds()));
    }
}
