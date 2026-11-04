package com.example.shain.application.service;

import com.example.auth.domain.service.InitialPasswordResolver;
import com.example.auth.domain.service.PasswordHasher;
import com.example.auth.domain.service.PasswordPolicy;
import com.example.auth.domain.service.TemporaryPasswordGenerator;
import com.example.auth.testsupport.FakeTransactionManager;
import com.example.auth.testsupport.InMemoryLoginPort;
import com.example.auth.testsupport.InMemoryShainPort;
import com.example.shain.application.port.in.UpdateShainUseCase.UpdateShainCommand;
import com.example.shain.domain.model.InvalidShainIdException;
import com.example.shain.domain.model.ShainId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShainServiceUpdateTest {

    private InMemoryShainPort shainPort;
    private InMemoryLoginPort loginPort;
    private ShainService service;

    @BeforeEach
    void setUp() {
        shainPort = new InMemoryShainPort();
        shainPort.add(126, "変更 太郎");
        shainPort.add(200, "既存 花子");
        loginPort = new InMemoryLoginPort();
        loginPort.put("126", "hash", false);
        loginPort.put("200", "hash", false);
        loginPort.put("900", "hash", false); // 社員はいないがアカウントだけあるID
        service = new ShainService(shainPort, loginPort, new PasswordHasher(),
                new InitialPasswordResolver(new PasswordPolicy(), new TemporaryPasswordGenerator()),
                new FakeTransactionManager(shainPort, loginPort));
    }

    private static UpdateShainCommand command(String newId) {
        return new UpdateShainCommand("126", newId, "変更 太郎", "ヘンコウ タロウ", 2020, "男", "一般");
    }

    @Test
    void 空いているIDに変更できる() {
        ShainId result = service.updateShain(command(" 300 "));

        assertEquals(ShainId.of(300), result);
        assertTrue(shainPort.findById(ShainId.of(300)).isPresent());
        assertTrue(shainPort.findById(ShainId.of(126)).isEmpty());
        assertEquals(List.of("126->300"), shainPort.changedIds);
        assertEquals("ヘンコウ タロウ", shainPort.findById(ShainId.of(300)).get().getNamekana());
    }

    @Test
    void IDを変えないときはIDの書き換えをしない() {
        assertEquals(ShainId.of(126), service.updateShain(command("126")));
        assertEquals(ShainId.of(126), service.updateShain(command(null)));
        assertTrue(shainPort.changedIds.isEmpty());
    }

    @Test
    void ほかの社員が使っているIDには変更できず_何も変わらない() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> service.updateShain(command("200")));

        assertEquals("社員ID 200 はすでにほかの社員が使っています。",
                InvalidShainIdException.findIn(e).orElseThrow().getMessage());
        assertTrue(shainPort.findById(ShainId.of(126)).isPresent());
        assertEquals("既存 花子", shainPort.findById(ShainId.of(200)).get().getName());
        assertTrue(shainPort.changedIds.isEmpty());
    }

    @Test
    void ログインアカウントだけ残っているIDにも変更できない() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> service.updateShain(command("900")));
        assertTrue(InvalidShainIdException.findIn(e).isPresent());
        assertTrue(shainPort.changedIds.isEmpty());
    }

    @Test
    void 数字以外や先頭0や10桁以上は使えない() {
        for (String bad : new String[] {"abc", "12a", "0126", "-5", "1234567890", " "}) {
            assertTrue(service.checkNewId("126", bad).isPresent(), bad);
        }
        assertEquals(Optional.empty(), service.checkNewId("126", "123456789"));
    }

    @Test
    void 入力中チェックは今のIDなら問題なしとする() {
        assertEquals(Optional.empty(), service.checkNewId("126", "126"));
        assertEquals(Optional.empty(), service.checkNewId("126", "301"));
        assertTrue(service.checkNewId("126", "200").get().contains("すでにほかの社員"));
        assertTrue(service.checkNewId("126", "900").get().contains("ログインアカウント"));
    }
}
