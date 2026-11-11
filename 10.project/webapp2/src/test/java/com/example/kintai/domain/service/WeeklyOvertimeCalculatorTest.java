package com.example.kintai.domain.service;

import com.example.kintai.domain.service.WeeklyOvertimeCalculator.Day;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 週40時間の時間外（休日出勤の日の分） */
class WeeklyOvertimeCalculatorTest {

    /** 2026/11/8(日)〜11/14(土) の週 */
    private static final LocalDate SUN = LocalDate.of(2026, 11, 8);

    private static Day weekday(int offset, int hours, int minutes) {
        return new Day(SUN.plusDays(offset), hours * 60 + minutes, false, 0);
    }

    private static Day holiday(int offset, int minutes) {
        return new Day(SUN.plusDays(offset), minutes, true, 0);
    }

    @Test
    void 週の区切りは日曜から土曜() {
        assertEquals(SUN, WeeklyOvertimeCalculator.weekStart(LocalDate.of(2026, 11, 14)));
        assertEquals(SUN, WeeklyOvertimeCalculator.weekStart(SUN));
        assertEquals(LocalDate.of(2026, 11, 15), WeeklyOvertimeCalculator.weekStart(LocalDate.of(2026, 11, 15)));
    }

    @Test
    void 月から金で40時間なら土曜の休出は全部時間外() {
        // 画面の例：月 9時間（時間外1時間）、火〜金 8時間、土 2時間 → 土曜の時間外 2時間
        List<Day> week = List.of(holiday(0, 0), weekday(1, 9, 0), weekday(2, 8, 0), weekday(3, 8, 0),
                weekday(4, 8, 0), weekday(5, 8, 0), holiday(6, 120));

        Map<LocalDate, Integer> ot = WeeklyOvertimeCalculator.holidayOvertime(week);

        assertEquals(120, ot.get(SUN.plusDays(6)));
        assertEquals(0, ot.get(SUN));
    }

    @Test
    void 平日が40時間に足りなければ休出で埋めた残りが時間外() {
        // 月〜金のうち1日が6時間（38時間）→ 土曜4時間のうち2時間は40時間に足し、時間外2時間
        List<Day> week = List.of(weekday(1, 8, 0), weekday(2, 6, 0), weekday(3, 8, 0), weekday(4, 8, 0),
                weekday(5, 8, 0), holiday(6, 240));

        assertEquals(120, WeeklyOvertimeCalculator.holidayOvertime(week).get(SUN.plusDays(6)));
    }

    @Test
    void 祝日は0時間_有給は8時間として数える() {
        // 月は祝日（休み）、水は有給、ほかは8時間 → 32時間。土曜の3時間は全部40時間に足し、時間外なし
        List<Day> week = new ArrayList<>(List.of(holiday(1, 0), weekday(2, 8, 0),
                new Day(SUN.plusDays(3), 0, false, 480), weekday(4, 8, 0), weekday(5, 8, 0), holiday(6, 180)));

        assertEquals(0, WeeklyOvertimeCalculator.holidayOvertime(week).get(SUN.plusDays(6)));
    }

    @Test
    void 祝日のある週の土曜は40時間に足りない分まで時間外にならない() {
        // 2026/11/1〜7：月 7:45、火（文化の日）0、水・木 8時間、金 7:45 → 31:30。土曜 2時間は時間外なし
        List<Day> week = List.of(weekday(1, 7, 45), holiday(2, 0), weekday(3, 10, 0), weekday(4, 13, 0),
                weekday(5, 7, 45), holiday(6, 120));

        assertEquals(0, WeeklyOvertimeCalculator.holidayOvertime(week).get(SUN.plusDays(6)));
    }

    @Test
    void 半休は4時間と働いた分で8時間まで() {
        // 水は半休で4時間働いた（4＋4＝8時間）→ 40時間に達している
        List<Day> week = List.of(weekday(1, 8, 0), weekday(2, 8, 0), new Day(SUN.plusDays(3), 240, false, 240),
                weekday(4, 8, 0), weekday(5, 8, 0), holiday(6, 60));

        assertEquals(60, WeeklyOvertimeCalculator.holidayOvertime(week).get(SUN.plusDays(6)));
    }

    @Test
    void 平日の祝日に働いた分も休出として数える() {
        // 月（祝日）に3時間働き、火〜金は8時間（32時間）→ 祝日の3時間は40時間に足し、時間外なし
        List<Day> week = List.of(holiday(1, 180), weekday(2, 8, 0), weekday(3, 8, 0), weekday(4, 8, 0), weekday(5, 8, 0));
        assertEquals(0, WeeklyOvertimeCalculator.holidayOvertime(week).get(SUN.plusDays(1)));

        // 祝日に10時間働いた → 8時間は40時間に足し、2時間は時間外
        List<Day> longDay = List.of(holiday(1, 600), weekday(2, 8, 0), weekday(3, 8, 0), weekday(4, 8, 0), weekday(5, 8, 0));
        assertEquals(120, WeeklyOvertimeCalculator.holidayOvertime(longDay).get(SUN.plusDays(1)));
    }

    @Test
    void 休出1日で40時間に足すのは8時間まで() {
        // 平日0時間の週に土曜10時間 → 8時間は40時間に足し、2時間は時間外
        assertEquals(120, WeeklyOvertimeCalculator.holidayOvertime(List.of(holiday(6, 600))).get(SUN.plusDays(6)));
    }
}
