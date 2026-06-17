package com.qnectdk.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qnectdk.global.exception.ErrorCode;

import java.util.Map;

/**
 * 모든 API의 공통 응답 포맷.
 * 성공: { "success": true, "data": {...}, "error": null }
 * 실패: { "success": false, "data": null, "error": { "code": "...", "message": "..." } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ErrorDetail error) {

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
    public record ErrorDetail(String code, String message, Map<String, String> fields) {
    }
}
