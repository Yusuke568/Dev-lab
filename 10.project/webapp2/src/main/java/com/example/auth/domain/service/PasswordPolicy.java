package com.example.auth.domain.service;

import java.util.ArrayList;
import java.util.List;

/**
 * 新しいパスワードの決まり。
 */
public class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    /**
     * 新しいパスワードをチェックし、問題があればエラーメッセージの一覧を返す（空なら問題なし）。
     *
     * @param currentPassword 本人が変更する場合の現在のパスワード。管理者が設定する場合は null
     */
    public List<String> validate(String newPassword, String confirm, String currentPassword) {
        List<String> errors = new ArrayList<>();
        if (newPassword == null || newPassword.isEmpty()) {
            errors.add("新しいパスワードを入力してください。");
            return errors;
        }
        if (newPassword.length() < MIN_LENGTH) {
            errors.add("新しいパスワードは" + MIN_LENGTH + "文字以上で入力してください。");
        }
        if (!newPassword.equals(confirm)) {
            errors.add("新しいパスワードと確認用のパスワードが一致しません。");
        }
        if (currentPassword != null && newPassword.equals(currentPassword)) {
            errors.add("新しいパスワードには、現在のパスワードと違うものを指定してください。");
        }
        return errors;
    }
}
