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
    void 日またぎでも昼休みを引き_時間外は8時間を超えた分() {
        // 9:00〜翌1:00 は16時間。昼休み1時間を引いて15時間
        AttendanceRecord record = record("09:00", "01:00");

        assertEquals(Duration.ofHours(15), record.calculateRawWorkDuration());
    }

    @Test
    void 夜勤は昼休みに重ならないので引かない() {
        AttendanceRecord record = record("22:00", "06:00");

        assertEquals(Duration.ofHours(8), record.calculateRawWorkDuration());
    }

    @Test
    void 翌日の昼休みに重なる分も引く() {
        // 13:00〜翌13:00 ではなく 13:00〜翌12:30（23時間30分）。翌日の12:00〜12:30が昼休みに重なる
        AttendanceRecord record = record("13:00", "12:30");

        assertEquals(Duration.ofMinutes(23 * 60 + 30 - 30), record.calculateRawWorkDuration());
    }
}
