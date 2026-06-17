package com.qnectdk.global.util;

import java.time.LocalDate;

/**
 * 생년월일 → 띠 동물. 공식: (생년 - 4) % 12.
 * DB에 저장하지 않고 응답 시 계산한다.
 */
public final class ZodiacUtil {

    private static final String[] ZODIAC = {
            "쥐", "소", "호랑이", "토끼", "용", "뱀", "말", "양", "원숭이", "닭", "개", "돼지"
    };

    private ZodiacUtil() {
    }

    public static String of(LocalDate birthDate) {
        int index = Math.floorMod(birthDate.getYear() - 4, ZODIAC.length);
        return ZODIAC[index];
    }
}
