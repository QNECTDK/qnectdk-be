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
 * <p>서버 측 고정 정의로 항상 동일한 17종·동일 식별자(character01~character17)를 제공한다.
 * {@code characterId -> imageUrl} 매핑은 이 enum 한 곳에서만 조회한다(중복 정의 금지).
 */
@Schema(description = "프로필 캐릭터 카탈로그(17종)")
@Getter
public enum CharacterImage {
    CHARACTER_01("character01", "https://cdn.qnect.example.com/characters/01.png"),
    CHARACTER_02("character02", "https://cdn.qnect.example.com/characters/02.png"),
    CHARACTER_03("character03", "https://cdn.qnect.example.com/characters/03.png"),
    CHARACTER_04("character04", "https://cdn.qnect.example.com/characters/04.png"),
    CHARACTER_05("character05", "https://cdn.qnect.example.com/characters/05.png"),
    CHARACTER_06("character06", "https://cdn.qnect.example.com/characters/06.png"),
    CHARACTER_07("character07", "https://cdn.qnect.example.com/characters/07.png"),
    CHARACTER_08("character08", "https://cdn.qnect.example.com/characters/08.png"),
    CHARACTER_09("character09", "https://cdn.qnect.example.com/characters/09.png"),
    CHARACTER_10("character10", "https://cdn.qnect.example.com/characters/10.png"),
    CHARACTER_11("character11", "https://cdn.qnect.example.com/characters/11.png"),
    CHARACTER_12("character12", "https://cdn.qnect.example.com/characters/12.png"),
    CHARACTER_13("character13", "https://cdn.qnect.example.com/characters/13.png"),
    CHARACTER_14("character14", "https://cdn.qnect.example.com/characters/14.png"),
    CHARACTER_15("character15", "https://cdn.qnect.example.com/characters/15.png"),
    CHARACTER_16("character16", "https://cdn.qnect.example.com/characters/16.png"),
    CHARACTER_17("character17", "https://cdn.qnect.example.com/characters/17.png");

    private final String characterId;
    private final String imageUrl;

    CharacterImage(String characterId, String imageUrl) {
        this.characterId = characterId;
        this.imageUrl = imageUrl;
    }

    /** 카탈로그 전체(17종)를 응답 DTO로 변환해 반환한다. */
    public static List<CharacterResponse> all() {
        return Arrays.stream(values())
                .map(c -> new CharacterResponse(c.characterId, c.imageUrl))
                .toList();
    }

    /** 식별자와 일치하는 카탈로그 항목을 반환한다. 없으면 빈 Optional. */
    public static Optional<CharacterImage> findById(String characterId) {
        return Arrays.stream(values())
                .filter(c -> c.characterId.equals(characterId))
                .findFirst();
    }
}
