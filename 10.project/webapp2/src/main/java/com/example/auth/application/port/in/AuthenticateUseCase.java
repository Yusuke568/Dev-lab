package com.example.auth.application.port.in;

/**
 * 認証ユースケース。
 */
public interface AuthenticateUseCase {
    boolean authenticate(String username, String password);

    /**
     * 次回ログイン時にパスワードの変更が必要か（管理者が初期化した、または初期パスワードのまま）。
     */
    boolean isPasswordChangeRequired(String username);
}
