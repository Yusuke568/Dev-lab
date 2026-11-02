package com.example.auth.domain.port.out;

import com.example.auth.domain.model.LoginAccount;

import java.util.Optional;

/**
 * ログイン認証のための永続化操作を定義するポート。
 */
public interface LoginPort {
    /**
     * 有効なユーザーの保存済みパスワードハッシュを取得します。
     * ユーザーが存在しない、または無効な場合は空を返します。
     */
    Optional<String> findPasswordHash(String username);

    /**
     * アカウントを取得します（無効なアカウントも含む）。
     */
    Optional<LoginAccount> findAccount(String username);

    /**
     * 同じユーザー名のアカウントがあるか確認します。
     */
    boolean exists(String username);

    /**
     * 有効なアカウントを作成します。passwordHash はハッシュ化済みの値を渡します。
     */
    void insertAccount(String username, String passwordHash, boolean mustChangePassword);

    /**
     * パスワードと「次回ログイン時に変更させる」フラグを更新します。passwordHash はハッシュ化済みの値を渡します。
     */
    void updatePassword(String username, String passwordHash, boolean mustChangePassword);
}
