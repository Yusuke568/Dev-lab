package com.example.auth.application.service;

import com.example.auth.application.port.in.AuthenticateUseCase;
import com.example.auth.domain.port.out.LoginPort;

/**
 * 認証サービスの実装。
 */
public class AuthenticateService implements AuthenticateUseCase {

    private final LoginPort loginPort;

    public AuthenticateService(LoginPort loginPort) {
        this.loginPort = loginPort;
    }

    @Override
    public boolean authenticate(String username, String password) {
        return loginPort.authenticate(username, password);
    }
}
