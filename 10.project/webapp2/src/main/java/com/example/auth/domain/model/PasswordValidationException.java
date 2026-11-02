package com.example.auth.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * パスワードの変更・設定で、入力内容に問題があったときの例外。
 * 画面にそのまま表示できる日本語のメッセージを持つ。パスワードそのものは含めない。
 */
public class PasswordValidationException extends RuntimeException {

    private final List<String> errors;

    public PasswordValidationException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public PasswordValidationException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }

    /**
     * TransactionManager などで包まれた例外の中から、この例外を探す。
     */
    public static Optional<PasswordValidationException> findIn(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof PasswordValidationException) {
                return Optional.of((PasswordValidationException) t);
            }
        }
        return Optional.empty();
    }
}
