package com.example.kintai.domain.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 内閣府が公表している祝日の一覧と照らし合わせる */
class JapaneseHolidayCalendarTest {

    private static List<String> dates(int year) {
        return JapaneseHolidayCalendar.holidaysOf(year).keySet().stream().map(LocalDate::toString).toList();
    }

    @Test
    void 年2026年の祝日() {
        assertEquals(List.of(
                "2026-01-01", "2026-01-12", "2026-02-11", "2026-02-23", "2026-03-20", "2026-04-29",
                "2026-05-03", "2026-05-04", "2026-05-05", "2026-05-06", "2026-07-20", "2026-08-11",
                "2026-09-21", "2026-09-22", "2026-09-23", "2026-10-12", "2026-11-03", "2026-11-23"), dates(2026));
        assertEquals(Optional.of("振替休日"), JapaneseHolidayCalendar.nameOf(LocalDate.of(2026, 5, 6)));
        assertEquals(Optional.of("国民の休日"), JapaneseHolidayCalendar.nameOf(LocalDate.of(2026, 9, 22)));
        assertEquals(Optional.of("文化の日"), JapaneseHolidayCalendar.nameOf(LocalDate.of(2026, 11, 3)));
    }

    @Test
    void 年2025年は日曜の祝日の翌日が振替休日() {
        assertEquals(List.of(
                "2025-01-01", "2025-01-13", "2025-02-11", "2025-02-23", "2025-02-24", "2025-03-20",
                "2025-04-29", "2025-05-03", "2025-05-04", "2025-05-05", "2025-05-06", "2025-07-21",
                "2025-08-11", "2025-09-15", "2025-09-23", "2025-10-13", "2025-11-03", "2025-11-23",
                "2025-11-24"), dates(2025));
    }

    @Test
    void 年2019年の即位関連の休日() {
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2019, 4, 30)), "国民の休日");
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2019, 5, 1)), "即位の日");
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2019, 5, 2)), "国民の休日");
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2019, 5, 6)), "振替休日");
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2019, 10, 22)), "即位礼正殿の儀");
        assertFalse(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2019, 12, 23)), "2019年の天皇誕生日はない");
    }

    @Test
    void 東京五輪の年は海の日などが移動している() {
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2020, 7, 23)));
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2020, 7, 24)));
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2020, 8, 10)));
        assertFalse(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2020, 10, 12)));
        assertTrue(JapaneseHolidayCalendar.isHoliday(LocalDate.of(2021, 8, 9)), "山の日(8/8 日曜)の振替休日");
    }

    @Test
    void 月ごとの祝日を名前つきで返す() {
        Map<LocalDate, String> november = JapaneseHolidayCalendar.holidaysOf(YearMonth.of(2026, 11));

        assertEquals(Map.of(LocalDate.of(2026, 11, 3), "文化の日", LocalDate.of(2026, 11, 23), "勤労感謝の日"), november);
    }
}
