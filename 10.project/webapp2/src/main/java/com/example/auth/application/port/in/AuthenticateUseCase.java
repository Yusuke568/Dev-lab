package com.example.auth.application.port.in;

/**
 * 認証ユースケース。
 */
public interface AuthenticateUseCase {
    boolean authenticate(String username, String password);
}
