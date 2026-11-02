package com.example.auth.application.port.in;

/**
 * ログインしている本人がパスワードを変更するユースケース。
 */
public interface ChangeOwnPasswordUseCase {

    /**
     * パスワードを変更し、「次回ログイン時に変更させる」フラグを下ろします。
     *
     * @param username ログインしている本人のユーザー名（リクエストのパラメータではなくセッションから渡す）
     * @throws com.example.auth.domain.model.PasswordValidationException 入力内容に問題があるとき
     */
    void changePassword(String username, String currentPassword, String newPassword, String confirm);
}
