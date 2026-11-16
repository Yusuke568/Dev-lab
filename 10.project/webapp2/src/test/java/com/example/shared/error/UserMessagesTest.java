package com.example.shared.error;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMessagesTest {

    private static final String FALLBACK = "処理できませんでした。";

    @Test
    void 業務のメッセージはそのまま出す() {
        assertEquals("申請中の記録のみ承認・却下できます。",
                UserMessages.of(new IllegalStateException("申請中の記録のみ承認・却下できます。"), FALLBACK));
    }

    @Test
    void トランザクションに包まれた業務のメッセージも出す() {
        Exception wrapped = new RuntimeException("トランザクションの実行中にエラーが発生しました",
                new IllegalArgumentException("この日はすでに申請しています。"));

        assertEquals("この日はすでに申請しています。", UserMessages.of(wrapped, FALLBACK));
    }

    @Test
    void DBのエラーは出さない() {
        Exception db = new RuntimeException("Failed to update: UPDATE staff_work SET ... WHERE id = ?",
                new SQLException("ERROR: relation \"staff_work\" does not exist"));

        assertEquals(FALLBACK, UserMessages.of(db, FALLBACK));
    }

    @Test
    void ライブラリの英語のメッセージは出さない() {
        assertEquals(FALLBACK, UserMessages.of(new IllegalArgumentException("For input string: \"abc\""), FALLBACK));
        assertEquals(FALLBACK, UserMessages.of(new IllegalStateException("Failed to hash password"), FALLBACK));
    }

    @Test
    void ファイルのエラーは日本語でも業務のメッセージでなければ出さない() {
        Exception io = new RuntimeException("作業報告書の生成に失敗しました。",
                new java.io.IOException("/srv/kintai/templates/work_report_template.xlsm (Permission denied)"));

        assertEquals(FALLBACK, UserMessages.of(io, FALLBACK));
    }

    @Test
    void 原因が輪になっていても止まる() {
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b", a);
        a.initCause(b);

        assertEquals(FALLBACK, UserMessages.of(a, FALLBACK));
    }
}
