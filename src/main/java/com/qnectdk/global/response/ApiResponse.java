package com.qnectdk.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qnectdk.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * 모든 API의 공통 응답 포맷.
 * 성공: { "success": true, "data": {...}, "error": null }
 * 실패: { "success": false, "data": null, "error": { "code": "...", "message": "..." } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,
        @Schema(description = "성공 시 응답 데이터 (실패 시 null)")
        T data,
        @Schema(description = "실패 시 에러 정보 (성공 시 null)")
        ErrorDetail error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorCode code) {
        return new ApiResponse<>(false, null, new ErrorDetail(code.name(), code.getMessage(), null));
    }

    public static ApiResponse<Void> fail(ErrorCode code, Map<String, String> fieldErrors) {
        return new ApiResponse<>(false, null, new ErrorDetail(code.name(), code.getMessage(), fieldErrors));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
            @Schema(description = "에러 코드(예: INVALID_INPUT)", example = "INVALID_INPUT")
            String code,
            @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다.")
            String message,
            @Schema(description = "검증 실패 시 필드별 메시지 (해당 없으면 null)")
            Map<String, String> fields) {
    }
}
