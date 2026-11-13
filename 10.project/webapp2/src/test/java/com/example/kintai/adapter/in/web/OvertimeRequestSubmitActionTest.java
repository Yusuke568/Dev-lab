package com.example.kintai.adapter.in.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 残業事前申請の対象日（1日・まとめて申請） */
class OvertimeRequestSubmitActionTest {

    @Test
    void まとめて申請の日は日付の順に重なりなく読む() {
        assertEquals(List.of(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21), LocalDate.of(2026, 11, 25)),
                OvertimeRequestSubmitAction.parseDates("2026-11-25, 2026-11-20,2026-11-21,2026-11-20,", "2026-11-01"));
    }

    @Test
    void まとめて申請の日がなければ1日の対象日を使う() {
        assertEquals(List.of(LocalDate.of(2026, 11, 7)), OvertimeRequestSubmitAction.parseDates("", "2026-11-07"));
        assertEquals(List.of(LocalDate.of(2026, 11, 7)), OvertimeRequestSubmitAction.parseDates(null, "2026-11-07"));
    }

    @Test
    void 対象日がない_正しくない_多すぎるときは申請できない() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> OvertimeRequestSubmitAction.parseDates("", ""))
                .getMessage().contains("対象日を入力"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> OvertimeRequestSubmitAction.parseDates("2026-11-31", null))
                .getMessage().contains("正しくありません"));
        StringBuilder many = new StringBuilder();
        LocalDate d = LocalDate.of(2026, 10, 1);
        for (int i = 0; i < 32; i++) many.append(d.plusDays(i)).append(',');
        assertTrue(assertThrows(IllegalArgumentException.class, () -> OvertimeRequestSubmitAction.parseDates(many.toString(), null))
                .getMessage().contains("31 日まで"));
    }
}
