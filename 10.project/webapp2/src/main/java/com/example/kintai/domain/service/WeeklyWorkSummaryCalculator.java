package com.example.kintai.domain.service;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WeeklyWorkSummary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/**
 * 作業報告書Excelの「累計/残業時間」と同じ計算を、勤怠記録から行う。
 * 稼働時間は総勤務時間（休憩控除・補正・間接込み）、残業時間は日ごとの時間外（総勤務時間の8時間超え）。
 */
public final class WeeklyWorkSummaryCalculator {

    private WeeklyWorkSummaryCalculator() {
    }

    /**
     * @param yearMonth    作業報告書の月
     * @param weekEndDates 各週の水曜日（週は木〜水。第1週の木曜日は前月になることがある）
     * @param records      前月の月初から最終週の水曜日までの勤怠記録
     */
    public static List<WeeklyWorkSummary> calculate(YearMonth yearMonth, List<LocalDate> weekEndDates,
                                                    List<AttendanceRecord> records) {
        YearMonth previousMonth = yearMonth.minusMonths(1);
        long previousWork = sum(records, r -> YearMonth.from(r.getWorkDate()).equals(previousMonth), WeeklyWorkSummaryCalculator::workMinutes);
        long previousOvertime = sum(records, r -> YearMonth.from(r.getWorkDate()).equals(previousMonth), AttendanceRecord::getOvertimeMinutes);

        List<WeeklyWorkSummary> summaries = new ArrayList<>();
        for (LocalDate weekEnd : weekEndDates) {
            LocalDate rangeStart = weekEnd.minusDays(6);
            Predicate<AttendanceRecord> inWeek = r -> !r.getWorkDate().isBefore(rangeStart) && !r.getWorkDate().isAfter(weekEnd);
            LocalDate monthUntil = weekEnd.isAfter(yearMonth.atEndOfMonth()) ? yearMonth.atEndOfMonth() : weekEnd;
            Predicate<AttendanceRecord> inMonthSoFar = r -> YearMonth.from(r.getWorkDate()).equals(yearMonth)
                    && !r.getWorkDate().isAfter(monthUntil);

            summaries.add(new WeeklyWorkSummary(rangeStart, weekEnd,
                    sum(records, inWeek, WeeklyWorkSummaryCalculator::workMinutes),
                    sum(records, inWeek, AttendanceRecord::getOvertimeMinutes),
                    sum(records, inMonthSoFar, WeeklyWorkSummaryCalculator::workMinutes),
                    sum(records, inMonthSoFar, AttendanceRecord::getOvertimeMinutes),
                    previousWork, previousOvertime));
        }
        return summaries;
    }

    private static long workMinutes(AttendanceRecord record) {
        return record.calculateWorkDuration().toMinutes();
    }

    private static long sum(List<AttendanceRecord> records, Predicate<AttendanceRecord> condition,
                            ToLongFunction<AttendanceRecord> value) {
        return records.stream().filter(condition).mapToLong(value).sum();
    }
}
