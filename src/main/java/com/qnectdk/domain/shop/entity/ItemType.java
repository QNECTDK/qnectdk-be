package com.qnectdk.domain.shop.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상점 아이템 종류. 장착은 type별로 각각 하나씩만 가능.")
public enum ItemType {
    @Schema(description = "프로필 캐릭터 (200P). 미장착 시 생년월일 띠 캐릭터가 기본 표시") CHARACTER
}