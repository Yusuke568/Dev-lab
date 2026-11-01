package com.example.auth.application.service;

import com.example.auth.application.port.in.AuthenticateUseCase;
import com.example.auth.domain.port.out.LoginPort;
import com.example.auth.domain.service.PasswordHasher;

import java.util.Optional;

/**
 * 認証サービスの実装。
 */
public class AuthenticateService implements AuthenticateUseCase {

    private final LoginPort loginPort;
    private final PasswordHasher passwordHasher;
    /** ユーザーが存在しない場合も同じだけ時間をかけ、応答時間からユーザーの有無がわからないようにするためのハッシュ */
    private final String dummyHash;

    public AuthenticateService(LoginPort loginPort, PasswordHasher passwordHasher) {
        this.loginPort = loginPort;
        this.passwordHasher = passwordHasher;
        this.dummyHash = passwordHasher.hash("dummy-password".toCharArray());
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        Optional<String> storedHash = loginPort.findPasswordHash(username);
        boolean matched = passwordHasher.verify(password.toCharArray(), storedHash.orElse(dummyHash));
        return storedHash.isPresent() && matched;
    }
}
