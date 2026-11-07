package com.example.shain.adapter.in.web;

/**
 * 社員の登録・更新フォームの値の読み取り。
 */
final class ShainFormValues {

    private ShainFormValues() {
    }

    /**
     * 入社年（任意）。空欄なら 0（未設定）。
     * @throws IllegalArgumentException 数字でない、または 1900〜2100 の範囲外
     */
    static int entryYear(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            int year = Integer.parseInt(value.trim());
            if (year < 1900 || year > 2100) {
                throw new IllegalArgumentException("入社年は西暦（例：2020）で入れるか、空欄にしてください。");
            }
            return year;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("入社年は西暦（例：2020）で入れるか、空欄にしてください。");
        }
    }
}
