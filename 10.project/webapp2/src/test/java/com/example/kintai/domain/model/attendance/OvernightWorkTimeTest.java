package com.example.kintai.domain.model.attendance;

import com.example.kintai.domain.model.employee.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 日をまたいだ退勤（出勤より前の時刻の退勤）の扱い */
class OvernightWorkTimeTest {

    private static WorkTime time(String start, String end) {
        return new WorkTime(LocalTime.parse(start), LocalTime.parse(end));
    }

    private static AttendanceRecord record(String start, String end) {
        return new AttendanceRecord(new EmployeeId("126"), LocalDate.of(2026, 11, 2), time(start, end));
    }

    @Test
    void 退勤が出勤より前なら翌日の退勤として長さを数える() {
        WorkTime overnight = time("09:00", "01:00");

        assertTrue(overnight.isEndsNextDay());
        assertEquals(Duration.ofHours(16), overnight.getDuration());
        assertEquals(25 * 60, overnight.getEndMinuteFromStartDay());
    }

    @Test
    void 同じ日の退勤はこれまでどおり() {
        WorkTime sameDay = time("09:00", "18:00");

        assertFalse(sameDay.isEndsNextDay());
        assertEquals(Duration.ofHours(9), sameDay.getDuration());
    }

    @Test
    void 日またぎでも休憩時間帯を引く() {
        // 9:00〜翌1:00 は16時間。12:00〜13:00、18:00〜18:30、22:00〜22:30 の休憩2時間を引いて14時間
        AttendanceRecord record = record("09:00", "01:00");

        assertEquals(Duration.ofHours(14), record.calculateRawWorkDuration());
    }

    @Test
    void 夜勤は夜の休憩時間帯を引く() {
        // 22:00〜翌6:00 は8時間。22:00〜22:30、2:00〜3:00、5:00〜5:30 の休憩2時間を引いて6時間
        AttendanceRecord record = record("22:00", "06:00");

        assertEquals(Duration.ofHours(6), record.calculateRawWorkDuration());
    }

    @Test
    void 翌日の休憩時間帯に重なる分も引く() {
        // 13:00〜翌12:30（23時間30分）。18:00〜18:30、22:00〜22:30、2:00〜3:00、5:00〜5:30、8:30〜9:00、
        // 翌日の12:00〜12:30 の休憩3時間30分を引いて20時間
        AttendanceRecord record = record("13:00", "12:30");

        assertEquals(Duration.ofHours(20), record.calculateRawWorkDuration());
    }
}
