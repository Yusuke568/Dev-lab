package com.example.auth.domain.service;

import java.util.Set;

/**
 * 次回ログイン時のパスワード変更を強制されている間に、使ってよいアクションを決める。
 */
public final class ForcedPasswordChangePolicy {

    /** 変更が終わるまで使えるのは、パスワード変更とログアウトだけ */
    private static final Set<String> ALLOWED_ACTIONS = Set.of("PasswordChange", "PasswordChangeExecute", "Logout");

    private ForcedPasswordChangePolicy() {
    }

    public static boolean isAllowed(String actionName, boolean mustChangePassword) {
        return !mustChangePassword || ALLOWED_ACTIONS.contains(actionName);
    }
}
