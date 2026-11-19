package com.example.kintai.domain.service;

import com.example.kintai.domain.service.StaffWorkSummaryCalculator.DayRow;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator.RequestRow;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator.Summary;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 部下のサマリー（当月と年間） */
class StaffWorkSummaryCalculatorTest {

    private static final YearMonth NOV = YearMonth.of(2026, 11);
    private static final int PAID = 10;
    private static final int HALF = 11;
    private static final Map<Integer, Double> LEAVE = Map.of(PAID, 1.0, HALF, 0.5);

    private static DayRow work(String date, int totalHours, int overtimeHours) {
        return new DayRow(LocalDate.parse(date), true, totalHours * 60, overtimeHours * 60, null);
    }

    private static DayRow leave(String date, int abstractId) {
        return new DayRow(LocalDate.parse(date), false, 0, 0, abstractId);
    }

    private static Summary summarize(List<DayRow> days, List<RequestRow> requests) {
        return StaffWorkSummaryCalculator.summarize(NOV, days, requests, LEAVE,
                StaffWorkSummaryCalculator::isWeekend, 12.5);
    }

    @Test
    void 当月と年間_1月から表示する月までを集計する() {
        Summary s = summarize(List.of(
                work("2025-12-26", 10, 2),   // 前年は数えない
                work("2026-01-05", 10, 2),
                work("2026-10-01", 11, 3),
                work("2026-11-02", 9, 1),
                work("2026-11-07", 4, 4),    // 土曜 = 休日出勤
                work("2026-12-01", 10, 2)),  // 翌月以降は数えない
                List.of());
        assertEquals(13 * 60, s.month().totalMinutes());
        assertEquals(5 * 60, s.month().overtimeMinutes());
        assertEquals(2, s.month().workDays());
        assertEquals(1, s.month().holidayWorkDays());
        assertEquals(34 * 60, s.year().totalMinutes());
        assertEquals(10 * 60, s.year().overtimeMinutes());
        assertEquals(4, s.year().workDays());
        assertEquals(1, s.year().holidayWorkDays());
        int[] expected = new int[11];
        expected[0] = 120;
        expected[9] = 180;
        expected[10] = 300;
        assertArrayEquals(expected, s.overtimeByMonth());
    }

    @Test
    void 有給は勤務区分の日数で数え残日数はそのまま出す() {
        Summary s = summarize(List.of(
                leave("2026-03-02", PAID),
                leave("2026-11-04", PAID),
                leave("2026-11-05", HALF)), List.of());
        assertEquals(1.5, s.month().paidLeaveDays());
        assertEquals(2.5, s.year().paidLeaveDays());
        assertEquals(12.5, s.paidLeaveRemaining());
        assertEquals(0, s.month().workDays());

        Summary none = StaffWorkSummaryCalculator.summarize(NOV, List.of(), List.of(), LEAVE,
                StaffWorkSummaryCalculator::isWeekend, null);
        assertNull(none.paidLeaveRemaining());
    }

    @Test
    void 事前申請は当月の申請中と承認済みを数え_申請のない時間外の日を未申請にする() {
        Summary s = summarize(List.of(
                work("2026-11-02", 10, 2),
                work("2026-11-03", 10, 2),
                work("2026-11-04", 10, 2),
                work("2026-11-05", 10, 2),
                work("2026-11-06", 8, 0)),
                List.of(
                        new RequestRow(LocalDate.parse("2026-11-02"), "PENDING"),
                        new RequestRow(LocalDate.parse("2026-11-03"), "APPROVED"),
                        new RequestRow(LocalDate.parse("2026-11-04"), "REJECTED"),
                        new RequestRow(LocalDate.parse("2026-11-05"), "WITHDRAWN"),
                        new RequestRow(LocalDate.parse("2026-10-30"), "APPROVED")));
        assertEquals(1, s.pendingRequests());
        assertEquals(1, s.approvedRequests());
        assertEquals(2, s.unappliedDays());
    }

    @Test
    void 当月の時間外は実績に_まだ実績がない日の事前申請の予定を足して見込みにする() {
        Summary s = summarize(List.of(work("2026-11-02", 10, 2)),
                List.of(
                        new RequestRow(LocalDate.parse("2026-11-02"), "APPROVED", 180), // 実績がある日は実績で数える
                        new RequestRow(LocalDate.parse("2026-11-20"), "APPROVED", 120),
                        new RequestRow(LocalDate.parse("2026-11-21"), "PENDING", 60),
                        new RequestRow(LocalDate.parse("2026-11-24"), "WITHDRAWN", 300),
                        new RequestRow(LocalDate.parse("2026-11-25"), "REJECTED", 300)));
        assertEquals(120, s.month().overtimeMinutes());
        assertEquals(120, s.plannedApprovedMinutes());
        assertEquals(60, s.plannedPendingMinutes());
        assertEquals(300, s.monthOutlookMinutes());
        assertEquals(11, s.monthOvertimePercent()); // 5時間 ÷ 45時間
    }

    @Test
    void 上限との割合() {
        Summary s = summarize(List.of(work("2026-11-02", 44, 36)), List.of());
        assertEquals(80, s.monthOvertimePercent());
        assertEquals(7, s.yearOvertimePercent()); // 36時間 ÷ 540時間
    }

    @Test
    void 日曜の勤務は法定休出として時間外と別に数え_月100時間は足して見る() {
        // 11/8 は日曜（法定休日）。総勤務8時間は時間外0で、法定休出8時間
        Summary s = summarize(List.of(work("2026-11-02", 10, 2), work("2026-11-08", 8, 0)), List.of());
        assertEquals(120, s.month().overtimeMinutes());
        assertEquals(480, s.month().legalHolidayMinutes());
        assertEquals(480, s.year().legalHolidayMinutes());
        assertEquals(600, s.monthCombinedMinutes());
        assertEquals(10, s.monthCombinedPercent()); // 10時間 ÷ 100時間
        assertEquals(480, s.legalHolidayByMonth()[10]);
    }

    @Test
    void 時間外と法定休出の2から6か月の平均は一番高いものを出す() {
        // 10月：時間外90時間。11月：時間外70時間＋日曜（11/8）の法定休出20時間 → 2か月平均90時間で80時間超
        Summary s = summarize(List.of(work("2026-10-05", 98, 90), work("2026-11-02", 78, 70), work("2026-11-08", 20, 0)),
                List.of());
        assertEquals(2, s.maxAverage().months());
        assertEquals(90 * 60, s.maxAverage().minutes());
        assertEquals(113, s.averagePercent());
    }

    @Test
    void 平均には前年の月も入る_年間の集計には入らない() {
        // 2026年2月を表示：2025年12月の時間外60時間は平均に入るが、年間には入らない
        Summary s = StaffWorkSummaryCalculator.summarize(YearMonth.of(2026, 2),
                List.of(work("2025-12-01", 68, 60), work("2026-02-02", 68, 60)), List.of(), LEAVE,
                StaffWorkSummaryCalculator::isWeekend, null);
        assertEquals(60 * 60, s.year().overtimeMinutes());
        assertEquals(60 * 60, s.combinedLast6()[3]);  // 9月〜2月の4番目が12月
        assertEquals(40 * 60, s.maxAverage().minutes()); // 12月〜2月の3か月平均（60+0+60）÷3
        assertEquals(3, s.maxAverage().months());
    }
}
