package com.qnectdk.global.util;  // ← ZodiacUtil과 같은 패키지로 맞추세요

import java.time.LocalDate;

/**
 * 생년월일 → 기본 캐릭터 식별자(띠 기반).
 * 사용자가 캐릭터를 직접 설정하지 않았을 때 보여줄 기본값.
 * 띠 순서(쥐~돼지)가 character01~character12와 일치한다.
 */
public final class ZodiacCharacterUtil {

    private ZodiacCharacterUtil() {
    }

    /**
     * 생년월일로 띠 기본 캐릭터 id를 반환. 쥐띠→character01 ... 돼지띠→character12.
     */
    public static String defaultCharacterId(LocalDate birthDate) {
        int index = Math.floorMod(birthDate.getYear() - 4, 12); // 0=쥐 ... 11=돼지
        return String.format("character%02d", index + 1);        // character01 ~ character12
    }

    /**
     * 설정된 characterId가 있으면 그대로, 없으면(null/blank) 띠 기본값으로 채운다.
     */
    public static String resolve(String characterId, LocalDate birthDate) {
        if (characterId != null && !characterId.isBlank()) {
            return characterId;
        }
        return defaultCharacterId(birthDate);
    }
}