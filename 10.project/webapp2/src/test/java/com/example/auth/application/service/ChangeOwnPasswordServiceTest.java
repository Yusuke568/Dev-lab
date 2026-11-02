package com.example.auth.application.service;

import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.service.PasswordHasher;
import com.example.auth.domain.service.PasswordPolicy;
import com.example.auth.testsupport.InMemoryLoginPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeOwnPasswordServiceTest {

    private final PasswordHasher hasher = new PasswordHasher();
    private InMemoryLoginPort loginPort;
    private ChangeOwnPasswordService service;
    private String originalHash;

    @BeforeEach
    void setUp() {
        loginPort = new InMemoryLoginPort();
        originalHash = hasher.hash("current-pass".toCharArray());
        // 管理者が初期化した直後の状態
        loginPort.put("126", originalHash, true);
        service = new ChangeOwnPasswordService(loginPort, hasher, new PasswordPolicy());
    }

    @Test
    void 現在のパスワードが違うと変更できない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.changePassword("126", "wrong-pass", "new-password", "new-password"));

        assertTrue(e.getErrors().contains("現在のパスワードが正しくありません。"));
        assertEquals(originalHash, loginPort.get("126").getPasswordHash());
    }

    @Test
    void 確認用と一致しないと変更できない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.changePassword("126", "current-pass", "new-password", "new-passw0rd"));

        assertTrue(e.getErrors().contains("新しいパスワードと確認用のパスワードが一致しません。"));
        assertEquals(originalHash, loginPort.get("126").getPasswordHash());
    }

    @Test
    void 八文字より短いと変更できない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.changePassword("126", "current-pass", "short7c", "short7c"));

        assertTrue(e.getErrors().contains("新しいパスワードは8文字以上で入力してください。"));
        assertEquals(originalHash, loginPort.get("126").getPasswordHash());
    }

    @Test
    void 現在と同じパスワードには変更できない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.changePassword("126", "current-pass", "current-pass", "current-pass"));

        assertTrue(e.getErrors().contains("新しいパスワードには、現在のパスワードと違うものを指定してください。"));
        assertEquals(originalHash, loginPort.get("126").getPasswordHash());
    }

    @Test
    void 正しく入力すると変更され_変更強制フラグが下りる() {
        service.changePassword("126", "current-pass", "new-password", "new-password");

        String stored = loginPort.get("126").getPasswordHash();
        assertTrue(stored.startsWith("pbkdf2_sha256$"), "平文ではなくハッシュで保存する");
        assertTrue(hasher.verify("new-password".toCharArray(), stored));
        assertFalse(hasher.verify("current-pass".toCharArray(), stored));
        assertFalse(loginPort.get("126").isMustChangePassword());
    }

    @Test
    void ほかの社員のアカウントは変わらない() {
        String otherHash = hasher.hash("other-pass".toCharArray());
        loginPort.put("127", otherHash, false);

        service.changePassword("126", "current-pass", "new-password", "new-password");

        assertEquals(otherHash, loginPort.get("127").getPasswordHash());
    }
}
