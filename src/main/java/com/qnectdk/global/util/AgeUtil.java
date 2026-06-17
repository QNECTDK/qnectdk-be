package com.qnectdk.global.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * 생년월일 → 만 나이. DB에 저장하지 않고 응답 시 계산한다.
 */
public final class AgeUtil {

    private AgeUtil() {
    }

    public static int of(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
