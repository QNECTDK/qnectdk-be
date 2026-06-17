package com.qnectdk.global.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * QR/공유용 URL-safe 고유 코드 생성기. base62 10자.
 * 충돌 검증/재생성은 호출 측(repository를 가진 서비스)에서 처리한다.
 */
@Component
public class PublicCodeGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int CODE_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }
}
