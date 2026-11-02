package com.example.auth.domain.service;

import com.example.auth.domain.model.InitialPassword;
import com.example.auth.domain.model.PasswordValidationException;

import java.util.List;

/**
 * 管理者の指定（自動で作る／入力する）から、実際に設定するパスワードを決める。
 */
public class InitialPasswordResolver {

    private final PasswordPolicy passwordPolicy;
    private final TemporaryPasswordGenerator generator;

    public InitialPasswordResolver(PasswordPolicy passwordPolicy, TemporaryPasswordGenerator generator) {
        this.passwordPolicy = passwordPolicy;
        this.generator = generator;
    }

    /**
     * 設定するパスワードの平文を返す。入力されたパスワードが決まりに合わなければ例外にする。
     */
    public String resolve(InitialPassword initial) {
        if (initial == null || initial.isGenerate()) {
            return generator.generate();
        }
        List<String> errors = passwordPolicy.validate(initial.getPassword(), initial.getConfirm(), null);
        if (!errors.isEmpty()) {
            throw new PasswordValidationException(errors);
        }
        return initial.getPassword();
    }
}
