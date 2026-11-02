package com.example.auth.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForcedPasswordChangePolicyTest {

    @Test
    void フラグが立っている間はほかの画面に進めない() {
        for (String action : new String[] {"Menu", "KintaiDisplay", "ShainList", "ApprovalList", "MasterMenu",
                "PasswordResetExecute", "KintaiUpdateApi"}) {
            assertFalse(ForcedPasswordChangePolicy.isAllowed(action, true), action);
        }
    }

    @Test
    void フラグが立っていてもパスワード変更とログアウトは使える() {
        assertTrue(ForcedPasswordChangePolicy.isAllowed("PasswordChange", true));
        assertTrue(ForcedPasswordChangePolicy.isAllowed("PasswordChangeExecute", true));
        assertTrue(ForcedPasswordChangePolicy.isAllowed("Logout", true));
    }

    @Test
    void フラグが立っていなければどの画面にも進める() {
        assertTrue(ForcedPasswordChangePolicy.isAllowed("Menu", false));
        assertTrue(ForcedPasswordChangePolicy.isAllowed("KintaiDisplay", false));
    }
}
