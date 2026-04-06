package com.example.application.port.in;

/**
 * ユーザー認証を行うユースケース。
 */
public interface AuthenticateUseCase {
    /**
     * 指定されたユーザー名とパスワードで認証を行う。
     *
     * @param username ユーザー名
     * @param password パスワード
     * @return 認証に成功した場合はtrue、そうでない場合はfalse
     */
    boolean authenticate(String username, String password);
}
