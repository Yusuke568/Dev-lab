package com.example.auth.application.service;

import com.example.auth.domain.model.InitialPassword;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.service.InitialPasswordResolver;
import com.example.auth.domain.service.PasswordHasher;
import com.example.auth.domain.service.PasswordPolicy;
import com.example.auth.domain.service.TemporaryPasswordGenerator;
import com.example.auth.testsupport.InMemoryLoginPort;
import com.example.auth.testsupport.InMemoryShainPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResetPasswordServiceTest {

    private static final Set<String> ADMIN = Set.of("ATTENDANCE", "SHAIN_ADMIN");
    private static final Set<String> GENERAL = Set.of("ATTENDANCE", "OVERTIME_REQUEST", "LEAVE_REQUEST");

    private final PasswordHasher hasher = new PasswordHasher();
    private InMemoryLoginPort loginPort;
    private ResetPasswordService service;
    private String originalHash;

    @BeforeEach
    void setUp() {
        loginPort = new InMemoryLoginPort();
        InMemoryShainPort shainPort = new InMemoryShainPort();
        shainPort.add(126, "山田 太郎");
        shainPort.add(127, "佐藤 花子");
        originalHash = hasher.hash("old-password".toCharArray());
        loginPort.put("126", originalHash, false);
        service = new ResetPasswordService(loginPort, shainPort, hasher,
                new InitialPasswordResolver(new PasswordPolicy(), new TemporaryPasswordGenerator()));
    }

    @Test
    void SHAIN_ADMIN権限がないと拒否され_パスワードは変わらない() {
        assertThrows(SecurityException.class, () -> service.resetPassword(GENERAL, "126", InitialPassword.generated()));
        assertThrows(SecurityException.class, () -> service.resetPassword(null, "126", InitialPassword.generated()));

        assertEquals(originalHash, loginPort.get("126").getPasswordHash());
        assertEquals(false, loginPort.get("126").isMustChangePassword());
    }

    @Test
    void 仮パスワードは12文字で紛らわしい文字を含まず_それでログインできる() {
        String temporary = service.resetPassword(ADMIN, "126", InitialPassword.generated());

        assertEquals(12, temporary.length());
        assertTrue(temporary.matches("[A-Za-z2-9]{12}"), temporary);
        assertTrue(temporary.chars().noneMatch(c -> "0O1lI".indexOf(c) >= 0), temporary);
        String stored = loginPort.get("126").getPasswordHash();
        assertTrue(stored.startsWith("pbkdf2_sha256$"), "平文ではなくハッシュで保存する");
        assertTrue(hasher.verify(temporary.toCharArray(), stored));
    }

    @Test
    void 初期化すると次回ログイン時の変更強制フラグが立つ() {
        service.resetPassword(ADMIN, "126", InitialPassword.generated());

        assertTrue(loginPort.get("126").isMustChangePassword());
    }

    @Test
    void 管理者が指定したパスワードでも変更強制フラグが立ち_仮パスワードは返さない() {
        String temporary = service.resetPassword(ADMIN, "126", InitialPassword.specified("given-pass1", "given-pass1"));

        assertNull(temporary);
        assertTrue(hasher.verify("given-pass1".toCharArray(), loginPort.get("126").getPasswordHash()));
        assertTrue(loginPort.get("126").isMustChangePassword());
    }

    @Test
    void 指定したパスワードが短いと初期化しない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.resetPassword(ADMIN, "126", InitialPassword.specified("short", "short")));

        assertTrue(e.getErrors().contains("新しいパスワードは8文字以上で入力してください。"));
        assertEquals(originalHash, loginPort.get("126").getPasswordHash());
    }

    @Test
    void アカウントがない社員には社員IDをユーザー名にして作る() {
        String temporary = service.resetPassword(ADMIN, "127", InitialPassword.generated());

        assertTrue(hasher.verify(temporary.toCharArray(), loginPort.get("127").getPasswordHash()));
        assertTrue(loginPort.get("127").isMustChangePassword());
    }
}
