package com.example.kintai.domain.service;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.attendance.WeeklyWorkSummary;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeeklyWorkSummaryCalculatorTest {

    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private static AttendanceRecord day(int month, int dayOfMonth, String start, String end, int overtimeMinutes) {
        AttendanceRecord record = new AttendanceRecord(new EmployeeId("126"), LocalDate.of(2026, month, dayOfMonth),
                new WorkTime(LocalTime.parse(start), LocalTime.parse(end)));
        record.setOvertimeMinutes(overtimeMinutes);
        return record;
    }

    private static List<WeeklyWorkSummary> calculate(List<AttendanceRecord> records) {
        List<LocalDate> weekEnds = WeeklyWorkReport.resolveWeekRanges(NOVEMBER).stream().map(r -> r[1]).toList();
        return WeeklyWorkSummaryCalculator.calculate(NOVEMBER, weekEnds, records);
    }

    private final List<AttendanceRecord> records = List.of(
            day(10, 1, "09:00", "18:00", 0),    // 前月（第1週の外）
            day(10, 30, "09:00", "18:00", 0),   // 前月だが第1週（10/29〜11/4）に入る
            day(11, 2, "09:00", "20:00", 120),  // 休憩1時間を引いて10:00、残業2:00
            day(11, 4, "09:00", "18:00", 0),
            day(11, 5, "09:00", "18:00", 0));   // 第2週

    @Test
    void 第1週は前月の木曜日から数え_月の累計は月初から数える() {
        WeeklyWorkSummary first = calculate(records).get(0);

        assertEquals("・10/29-11/4の累計/残業時間", first.getHeadingLine());
        assertEquals("週26:00/2:00,月18:00/2:00", first.getTotalsLine());
        assertEquals("(前月16:00/0:00)", first.getPreviousMonthLine());
    }

    @Test
    void 月の累計はその週の水曜日までを足していく() {
        List<WeeklyWorkSummary> summaries = calculate(records);

        assertEquals("週8:00/0:00,月26:00/2:00", summaries.get(1).getTotalsLine());
        assertEquals("・11/5-11/11の累計/残業時間", summaries.get(1).getHeadingLine());
    }

    @Test
    void 月末をまたぐ週の月の累計は月末までにする() {
        List<AttendanceRecord> withDecember = new java.util.ArrayList<>(records);
        withDecember.add(day(12, 1, "09:00", "18:00", 0));

        WeeklyWorkSummary last = calculate(withDecember).get(4);

        assertEquals("・11/26-12/2の累計/残業時間", last.getHeadingLine());
        assertEquals("週8:00/0:00,月26:00/2:00", last.getTotalsLine());
    }

    @Test
    void 勤怠がなければゼロで表示する() {
        WeeklyWorkSummary first = calculate(List.of()).get(0);

        assertEquals("週0:00/0:00,月0:00/0:00", first.getTotalsLine());
        assertEquals("(前月0:00/0:00)", first.getPreviousMonthLine());
    }
}
