package com.qnectdk.domain.profile.entity;

import com.qnectdk.domain.profile.dto.CharacterResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

/**
 * 프로필 이미지로 선택 가능한 캐릭터 카탈로그의 단일 출처.
 *
 * <p>
 * 서버 측 고정 정의로 항상 동일한 19종·동일 식별자(character01~character19)를 제공한다.
 * 상점 캐릭터 아이템(ShopItemSeeder)·프론트 캐릭터 매핑과 동일한 19종으로 맞춘다.
 * 실제 이미지 에셋은 프론트가 characterId로 매핑하므로 서버는 식별자만 다룬다(URL 미보유).
 */
@Schema(description = "프로필 캐릭터 카탈로그(19종)")
@Getter
public enum CharacterImage {
  CHARACTER_01("character01"),
  CHARACTER_02("character02"),
  CHARACTER_03("character03"),
  CHARACTER_04("character04"),
  CHARACTER_05("character05"),
  CHARACTER_06("character06"),
  CHARACTER_07("character07"),
  CHARACTER_08("character08"),
  CHARACTER_09("character09"),
  CHARACTER_10("character10"),
  CHARACTER_11("character11"),
  CHARACTER_12("character12"),
  CHARACTER_13("character13"),
  CHARACTER_14("character14"),
  CHARACTER_15("character15"),
  CHARACTER_16("character16"),
  CHARACTER_17("character17"),
  CHARACTER_18("character18"),
  CHARACTER_19("character19");

  private final String characterId;

  CharacterImage(String characterId) {
    this.characterId = characterId;
    }

    /** 카탈로그 전체(19종)를 응답 DTO로 변환해 반환한다. */
    public static List<CharacterResponse> all() {
        return Arrays.stream(values())
            .map(c -> new CharacterResponse(c.characterId))
                .toList();
    }

    /** 식별자와 일치하는 카탈로그 항목을 반환한다. 없으면 빈 Optional. */
    public static Optional<CharacterImage> findById(String characterId) {
        return Arrays.stream(values())
                .filter(c -> c.characterId.equals(characterId))
                .findFirst();
    }
}
