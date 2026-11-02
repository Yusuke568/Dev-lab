package com.example.auth.application.service;

import com.example.auth.application.port.in.ResetPasswordUseCase;
import com.example.auth.domain.model.InitialPassword;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.port.out.LoginPort;
import com.example.auth.domain.service.InitialPasswordResolver;
import com.example.auth.domain.service.PasswordHasher;
import com.example.permission.domain.model.Feature;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;

import java.util.Set;

/**
 * 管理者によるパスワード初期化の実装。
 */
public class ResetPasswordService implements ResetPasswordUseCase {

    private final LoginPort loginPort;
    private final ShainPort shainPort;
    private final PasswordHasher passwordHasher;
    private final InitialPasswordResolver initialPasswordResolver;

    public ResetPasswordService(LoginPort loginPort, ShainPort shainPort, PasswordHasher passwordHasher,
                                InitialPasswordResolver initialPasswordResolver) {
        this.loginPort = loginPort;
        this.shainPort = shainPort;
        this.passwordHasher = passwordHasher;
        this.initialPasswordResolver = initialPasswordResolver;
    }

    @Override
    public String resetPassword(Set<String> operatorFeatures, String staffId, InitialPassword initialPassword) {
        // 画面のボタン表示と FrontController に加えて、ここでも権限を確かめる
        if (operatorFeatures == null || !operatorFeatures.contains(Feature.SHAIN_ADMIN.name())) {
            throw new SecurityException("パスワードを初期化する権限がありません。");
        }
        ShainId id = new ShainId(staffId);
        if (shainPort.findById(id).isEmpty()) {
            throw new PasswordValidationException("社員ID " + staffId + " の社員が見つかりません。");
        }

        String plain = initialPasswordResolver.resolve(initialPassword);
        String hash = passwordHasher.hash(plain.toCharArray());
        String username = id.getValue();
        if (loginPort.exists(username)) {
            loginPort.updatePassword(username, hash, true);
        } else {
            loginPort.insertAccount(username, hash, true);
        }
        return initialPassword == null || initialPassword.isGenerate() ? plain : null;
    }
}
