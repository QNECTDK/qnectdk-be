package com.qnectdk.domain.user.dto;

import com.qnectdk.domain.user.entity.User;

public record SignupResponse(Long userId, String loginId, String phone, String name, String publicCode) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(), user.getLoginId(), user.getPhone(), user.getName(), user.getPublicCode());
    }
}
