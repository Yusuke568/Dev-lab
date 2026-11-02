package com.example.auth.domain.service;

import java.security.SecureRandom;

/**
 * 管理者が初期化したときなどに渡す、ランダムな仮パスワードを作る。
 * 読み間違えやすい 0 / O / 1 / l / I は使わず、大文字・小文字・数字を必ず1文字以上含める。
 */
public class TemporaryPasswordGenerator {

    public static final int LENGTH = 12;
    static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    static final String DIGITS = "23456789";
    static final String ALL = UPPER + LOWER + DIGITS;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        char[] chars = new char[LENGTH];
        chars[0] = pick(UPPER);
        chars[1] = pick(LOWER);
        chars[2] = pick(DIGITS);
        for (int i = 3; i < LENGTH; i++) {
            chars[i] = pick(ALL);
        }
        // 先頭3文字の種類が固定にならないよう並べ替える
        for (int i = LENGTH - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    private char pick(String source) {
        return source.charAt(random.nextInt(source.length()));
    }
}
