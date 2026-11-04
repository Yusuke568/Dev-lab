package com.example.kintai.domain.model.attendance;

import com.example.kintai.domain.model.employee.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 休憩時間帯：5:00〜5:30、8:30〜9:00、12:00〜13:00、18:00〜18:30、22:00〜22:30、2:00〜3:00 */
class BreakScheduleTest {

    private static Duration work(String start, String end) {
        return new AttendanceRecord(new EmployeeId("126"), LocalDate.of(2026, 11, 10),
                new WorkTime(LocalTime.parse(start), LocalTime.parse(end))).calculateRawWorkDuration();
    }

    @Test
    void 定時の9時から18時は昼休みだけ引いて8時間() {
        assertEquals(Duration.ofHours(8), work("09:00", "18:00"));
    }

    @Test
    void 夕方の休憩も引く() {
        // 9:00〜20:00：12:00〜13:00 と 18:00〜18:30 を引いて 9時間30分
        assertEquals(Duration.ofMinutes(9 * 60 + 30), work("09:00", "20:00"));
        // 18:15 退勤は 18:00〜18:15 の15分だけ引く
        assertEquals(Duration.ofMinutes(8 * 60), work("09:00", "18:15"));
    }

    @Test
    void 朝の休憩も引く() {
        // 8:00〜17:00：8:30〜9:00 と 12:00〜13:00 を引いて 7時間30分
        assertEquals(Duration.ofMinutes(7 * 60 + 30), work("08:00", "17:00"));
    }

    @Test
    void 深夜の休憩も引く() {
        // 9:00〜23:00：12:00〜13:00、18:00〜18:30、22:00〜22:30 を引いて 12時間
        assertEquals(Duration.ofHours(12), work("09:00", "23:00"));
        // 1:00〜6:00：2:00〜3:00、5:00〜5:30 を引いて 3時間30分
        assertEquals(Duration.ofMinutes(3 * 60 + 30), work("01:00", "06:00"));
    }

    @Test
    void 画面用のJSONに6つの時間帯を出す() {
        assertEquals("[[\"05:00\",\"05:30\"],[\"08:30\",\"09:00\"],[\"12:00\",\"13:00\"],"
                + "[\"18:00\",\"18:30\"],[\"22:00\",\"22:30\"],[\"02:00\",\"03:00\"]]", BreakSchedule.toJson());
    }
}
