package com.qnectdk.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 내 관심사 일괄 설정. 빈 리스트는 전체 해제를 의미한다.
 */
public record InterestUpdateRequest(

        @Schema(description = "설정할 관심사 ID 목록 (빈 배열은 전체 해제)", example = "[1, 2, 11]")
        @NotNull
        List<Long> interestIds
) {
}
