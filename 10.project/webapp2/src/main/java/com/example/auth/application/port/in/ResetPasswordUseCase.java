package com.example.auth.application.port.in;

import com.example.auth.domain.model.InitialPassword;

import java.util.Set;

/**
 * 管理者が社員のパスワードを初期化するユースケース。
 */
public interface ResetPasswordUseCase {

    /**
     * パスワードを初期化し、次回ログイン時に本人に変更させるようにします。
     * ログイン用のアカウントがなければ、ユーザー名を社員IDにして作ります。
     *
     * @param operatorFeatures 操作する人が使える機能（Feature の名前）。SHAIN_ADMIN がなければ拒否する
     * @return 自動で作った仮パスワード。管理者が指定した場合は null
     * @throws SecurityException 権限がないとき
     * @throws com.example.auth.domain.model.PasswordValidationException 入力内容に問題があるとき
     */
    String resetPassword(Set<String> operatorFeatures, String staffId, InitialPassword initialPassword);
}
