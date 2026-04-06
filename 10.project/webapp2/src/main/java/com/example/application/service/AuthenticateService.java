package com.example.application.service;

import com.example.application.port.in.AuthenticateUseCase;
import com.example.application.port.out.LoginPort;

public class AuthenticateService implements AuthenticateUseCase {

    private final LoginPort loginPort;

    public AuthenticateService(LoginPort loginPort) {
        this.loginPort = loginPort;
    }

    @Override
    public boolean authenticate(String username, String password) {
        return loginPort.verifyCredentials(username, password);
    }
}
