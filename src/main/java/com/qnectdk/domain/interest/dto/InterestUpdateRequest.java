package com.qnectdk.domain.interest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 내 관심사 일괄 설정. 빈 리스트는 전체 해제를 의미한다.
 */
public record InterestUpdateRequest(

        @NotNull
        List<Long> interestIds
) {
}
