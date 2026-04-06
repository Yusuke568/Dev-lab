package com.example.application.port.out;

/**
 * ログイン情報に関する永続化ポート。
 */
public interface LoginPort {
    /**
     * ユーザー名とパスワードが一致するか確認する。
     *
     * @param username ユーザー名
     * @param password パスワード
     * @return 一致すればtrue、それ以外はfalse
     */
    boolean verifyCredentials(String username, String password);
}
