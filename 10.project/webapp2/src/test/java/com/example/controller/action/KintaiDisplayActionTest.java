package com.example.controller.action;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KintaiDisplayActionTest {

    @Test
    void 年月があればその月を表示する() {
        assertEquals(YearMonth.of(2026, 11), KintaiDisplayAction.parseYearMonth("2026", "11"));
        assertEquals(YearMonth.of(2026, 3), KintaiDisplayAction.parseYearMonth("2026", "03"));
    }

    @Test
    void 年月がないか正しくなければ今月を表示する() {
        assertEquals(YearMonth.now(), KintaiDisplayAction.parseYearMonth(null, null));
        assertEquals(YearMonth.now(), KintaiDisplayAction.parseYearMonth("", "11"));
        assertEquals(YearMonth.now(), KintaiDisplayAction.parseYearMonth("2026", "13"));
        assertEquals(YearMonth.now(), KintaiDisplayAction.parseYearMonth("abc", "1"));
    }
}
