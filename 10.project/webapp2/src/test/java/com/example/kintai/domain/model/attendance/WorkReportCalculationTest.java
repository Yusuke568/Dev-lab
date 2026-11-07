package com.example.kintai.domain.model.attendance;

import com.example.kintai.domain.model.employee.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 作業報告書と同じ計算になること。
 *   総勤務時間(e) ＝ 休憩控除後の実働 ＋ 補正(通) ＋ 補正(深) ＋ 勤務時間(間接)(d)
 *   勤務時間(直接)(f) ＝ e − d
 *   残業時間 ＝ e − 8:00
 */
class WorkReportCalculationTest {

    private static AttendanceRecord record(String start, String end, Integer us, Integer mid, Integer indirect) {
        AttendanceRecord r = new AttendanceRecord(new EmployeeId("126"), LocalDate.of(2026, 11, 2),
                new WorkTime(LocalTime.parse(start), LocalTime.parse(end)));
        r.setCorrectionUsTime(us);
        r.setCorrectionMidTime(mid);
        r.setIndirectTime(indirect);
        return r;
    }

    private static Duration hm(int hours, int minutes) {
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    @Test
    void 定時_補正0時15分で8時間_残業なし() {
        // 9:00〜17:45：8:45 − 昼休み1:00 ＝ 7:45、＋0:15 ＝ 8:00
        AttendanceRecord r = record("09:00", "17:45", 15, null, null);

        assertEquals(hm(8, 0), r.calculateWorkDuration());
        assertEquals(hm(8, 0), r.calculateDirectWorkDuration());
        assertEquals(0, r.calculateOvertimeMinutes());
    }

    @Test
    void 間接作業2時間は総勤務と残業に入り_直接には入らない() {
        // 7:45 ＋ 補正0:15 ＋ 間接2:00 ＝ 10:00、直接 ＝ 10:00 − 2:00 ＝ 8:00、残業 ＝ 2:00
        AttendanceRecord r = record("09:00", "17:45", 15, null, 120);

        assertEquals(hm(10, 0), r.calculateWorkDuration());
        assertEquals(hm(8, 0), r.calculateDirectWorkDuration());
        assertEquals(120, r.calculateOvertimeMinutes());
    }

    @Test
    void 補正は残業に含める_9時から23時_補正通深0時30分ずつで残業5時間() {
        // 14:00 − 休憩(12:00〜13:00、18:00〜18:30、22:00〜22:30) 2:00 ＝ 12:00、＋0:30 ＋0:30 ＝ 13:00
        AttendanceRecord r = record("09:00", "23:00", 30, 30, null);

        assertEquals(hm(12, 0), r.calculateRawWorkDuration());
        assertEquals(hm(13, 0), r.calculateWorkDuration());
        assertEquals(hm(13, 0), r.calculateDirectWorkDuration());
        assertEquals(5 * 60, r.calculateOvertimeMinutes());
    }

    @Test
    void 補正なしなら実働の8時間超えが残業() {
        AttendanceRecord r = record("09:00", "23:00", null, null, null);

        assertEquals(4 * 60, r.calculateOvertimeMinutes());
    }
}
