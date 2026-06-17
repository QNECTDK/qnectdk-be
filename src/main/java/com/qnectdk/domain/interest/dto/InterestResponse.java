package com.qnectdk.domain.interest.dto;

import com.qnectdk.domain.interest.entity.Interest;

public record InterestResponse(Long id, String category, String name) {

    public static InterestResponse from(Interest interest) {
        return new InterestResponse(interest.getId(), interest.getCategory(), interest.getName());
    }
}
