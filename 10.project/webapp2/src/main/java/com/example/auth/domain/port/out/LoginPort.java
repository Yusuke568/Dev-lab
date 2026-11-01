package com.example.auth.domain.port.out;

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
}
