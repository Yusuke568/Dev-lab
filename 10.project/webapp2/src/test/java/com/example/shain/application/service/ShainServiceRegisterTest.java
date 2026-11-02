package com.example.shain.application.service;

import com.example.auth.application.service.AuthenticateService;
import com.example.auth.domain.model.InitialPassword;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.service.InitialPasswordResolver;
import com.example.auth.domain.service.PasswordHasher;
import com.example.auth.domain.service.PasswordPolicy;
import com.example.auth.domain.service.TemporaryPasswordGenerator;
import com.example.auth.testsupport.FakeTransactionManager;
import com.example.auth.testsupport.InMemoryLoginPort;
import com.example.auth.testsupport.InMemoryShainPort;
import com.example.shain.application.port.in.RegisterShainUseCase.RegisterShainCommand;
import com.example.shain.application.port.in.RegisterShainUseCase.RegisterShainResult;
import com.example.shain.domain.model.ShainId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShainServiceRegisterTest {

    private final PasswordHasher hasher = new PasswordHasher();
    private InMemoryShainPort shainPort;
    private InMemoryLoginPort loginPort;
    private ShainService service;

    @BeforeEach
    void setUp() {
        shainPort = new InMemoryShainPort();
        shainPort.add(126, "既存 社員");
        loginPort = new InMemoryLoginPort();
        service = new ShainService(shainPort, loginPort, hasher,
                new InitialPasswordResolver(new PasswordPolicy(), new TemporaryPasswordGenerator()),
                new FakeTransactionManager(shainPort, loginPort));
    }

    private static RegisterShainCommand command(InitialPassword initial) {
        return new RegisterShainCommand("新人 一郎", "シンジン イチロウ", 2026, "男", "一般", 3, initial);
    }

    @Test
    void 指定した初期パスワードでログインでき_変更強制フラグが立つ() {
        RegisterShainResult result = service.registerShain(command(InitialPassword.specified("first-pass1", "first-pass1")));

        assertEquals(ShainId.of(127), result.getId());
        assertNull(result.getTemporaryPassword(), "指定した場合は仮パスワードを返さない");
        assertTrue(shainPort.findById(ShainId.of(127)).isPresent());
        assertEquals(3, shainPort.departmentOf(ShainId.of(127)));

        AuthenticateService authenticate = new AuthenticateService(loginPort, hasher);
        assertTrue(authenticate.authenticate("127", "first-pass1"));
        assertTrue(authenticate.isPasswordChangeRequired("127"));
        assertTrue(loginPort.get("127").getPasswordHash().startsWith("pbkdf2_sha256$"), "平文ではなくハッシュで保存する");
    }

    @Test
    void 自動で作った仮パスワードを返し_それでログインできる() {
        RegisterShainResult result = service.registerShain(command(InitialPassword.generated()));

        String temporary = result.getTemporaryPassword();
        assertEquals(12, temporary.length());
        assertTrue(new AuthenticateService(loginPort, hasher).authenticate("127", temporary));
        assertTrue(loginPort.get("127").isMustChangePassword());
    }

    @Test
    void 確認用と一致しないとエラーになり_何も登録しない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.registerShain(command(InitialPassword.specified("first-pass1", "first-pass2"))));

        assertTrue(e.getErrors().contains("新しいパスワードと確認用のパスワードが一致しません。"));
        assertTrue(shainPort.findById(ShainId.of(127)).isEmpty());
        assertEquals(0, loginPort.size());
    }

    @Test
    void 短すぎるとエラーになり_何も登録しない() {
        PasswordValidationException e = assertThrows(PasswordValidationException.class,
                () -> service.registerShain(command(InitialPassword.specified("short", "short"))));

        assertTrue(e.getErrors().contains("新しいパスワードは8文字以上で入力してください。"));
        assertTrue(shainPort.findById(ShainId.of(127)).isEmpty());
        assertEquals(0, loginPort.size());
    }

    @Test
    void 同じユーザー名のアカウントがあればエラーになり_社員も登録しない() {
        loginPort.put("127", hasher.hash("someone-else".toCharArray()), false);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> service.registerShain(command(InitialPassword.generated())));

        assertTrue(PasswordValidationException.findIn(e).isPresent());
        assertTrue(shainPort.findById(ShainId.of(127)).isEmpty());
    }

    @Test
    void アカウント作成に失敗したら社員の登録も取り消される() {
        loginPort.failOnInsert = true;

        assertThrows(RuntimeException.class, () -> service.registerShain(command(InitialPassword.generated())));

        assertTrue(shainPort.findById(ShainId.of(127)).isEmpty(), "社員の登録が残っていない");
        assertNull(shainPort.departmentOf(ShainId.of(127)), "部署の割り当ても残っていない");
        assertEquals(1, shainPort.findAll().size());
    }
}
