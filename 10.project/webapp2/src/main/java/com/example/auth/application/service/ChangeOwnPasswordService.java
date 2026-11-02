package com.example.auth.application.service;

import com.example.auth.application.port.in.ChangeOwnPasswordUseCase;
import com.example.auth.domain.model.LoginAccount;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.port.out.LoginPort;
import com.example.auth.domain.service.PasswordHasher;
import com.example.auth.domain.service.PasswordPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * 本人によるパスワード変更の実装。
 */
public class ChangeOwnPasswordService implements ChangeOwnPasswordUseCase {

    private final LoginPort loginPort;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;

    public ChangeOwnPasswordService(LoginPort loginPort, PasswordHasher passwordHasher, PasswordPolicy passwordPolicy) {
        this.loginPort = loginPort;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword, String confirm) {
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("ログインしていません。");
        }
        LoginAccount account = loginPort.findAccount(username).filter(LoginAccount::isEnabled)
                .orElseThrow(() -> new PasswordValidationException("ログイン用のアカウントが見つかりません。"));

        List<String> errors = new ArrayList<>();
        if (currentPassword == null || currentPassword.isEmpty()) {
            errors.add("現在のパスワードを入力してください。");
        } else if (!passwordHasher.verify(currentPassword.toCharArray(), account.getPasswordHash())) {
            errors.add("現在のパスワードが正しくありません。");
        }
        errors.addAll(passwordPolicy.validate(newPassword, confirm, currentPassword));
        if (!errors.isEmpty()) {
            throw new PasswordValidationException(errors);
        }

        loginPort.updatePassword(username, passwordHasher.hash(newPassword.toCharArray()), false);
    }
}
