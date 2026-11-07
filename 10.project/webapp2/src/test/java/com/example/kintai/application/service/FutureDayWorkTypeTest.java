package com.example.kintai.application.service;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** まだ来ていない日の勤務区分の表示 */
class FutureDayWorkTypeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 8);

    private static AttendanceRecord record(LocalDate date, Integer abstractId) {
        AttendanceRecord r = new AttendanceRecord(new EmployeeId("126"), date, null);
        r.setAbstractId(abstractId);
        return r;
    }

    @Test
    void 未到来で何も入っていない出勤は未設定にする() {
        assertNull(GetMonthlyAttendanceService.displayedWorkType(record(TODAY.plusDays(1), 1), TODAY));
    }

    @Test
    void 今日と過去の出勤はそのまま() {
        assertEquals(1, GetMonthlyAttendanceService.displayedWorkType(record(TODAY, 1), TODAY));
        assertEquals(1, GetMonthlyAttendanceService.displayedWorkType(record(TODAY.minusDays(3), 1), TODAY));
    }

    @Test
    void 未到来でも入力のある日や出勤以外の区分はそのまま() {
        AttendanceRecord withTime = new AttendanceRecord(new EmployeeId("126"), TODAY.plusDays(2),
                new WorkTime(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        withTime.setAbstractId(1);
        assertEquals(1, GetMonthlyAttendanceService.displayedWorkType(withTime, TODAY));

        AttendanceRecord withNote = record(TODAY.plusDays(2), 1);
        withNote.setWorkDescription("客先常駐");
        assertEquals(1, GetMonthlyAttendanceService.displayedWorkType(withNote, TODAY));

        // 休日・有給の予定はそのまま
        assertEquals(10, GetMonthlyAttendanceService.displayedWorkType(record(TODAY.plusDays(1), 10), TODAY));
        assertEquals(2, GetMonthlyAttendanceService.displayedWorkType(record(TODAY.plusDays(1), 2), TODAY));
    }
}
