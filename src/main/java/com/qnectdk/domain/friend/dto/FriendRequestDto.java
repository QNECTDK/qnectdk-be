package com.qnectdk.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 친구 추가(수락) 요청. QR/URL로 접속한 상대 프로필에서 '수락'을 누르면 호출한다.
 */
public record FriendRequestDto(
    @Schema(description = "친구로 추가할 상대방 userId", example = "2") @NotNull(message = "friendId는 필수입니다.") Long friendId
) {
}
