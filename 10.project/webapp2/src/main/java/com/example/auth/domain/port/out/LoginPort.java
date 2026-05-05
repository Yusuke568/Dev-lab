package com.example.auth.domain.port.out;

/**
 * ログイン認証のための永続化操作を定義するポート。
 */
public interface LoginPort {
    /**
     * IDとパスワードが一致するか確認します。
     */
    boolean authenticate(String username, String password);
}
