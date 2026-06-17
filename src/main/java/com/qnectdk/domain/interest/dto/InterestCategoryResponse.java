package com.qnectdk.domain.interest.dto;

import java.util.List;

public record InterestCategoryResponse(String category, List<InterestItem> interests) {
}
