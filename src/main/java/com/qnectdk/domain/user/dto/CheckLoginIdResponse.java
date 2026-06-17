package com.qnectdk.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckLoginIdResponse(
        @Schema(description = "아이디 사용 가능 여부 (true면 사용 가능)") boolean available) {
}
