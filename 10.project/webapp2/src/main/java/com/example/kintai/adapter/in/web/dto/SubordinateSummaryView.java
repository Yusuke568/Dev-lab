package com.example.kintai.adapter.in.web.dto;

import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator.Summary;

import java.util.ArrayList;
import java.util.List;

/**
 * 部下の勤怠表の画面に出す、部下1人分のサマリー（JSP から読む形に整えたもの）。
 */
public class SubordinateSummaryView {

    /** 月ごとの時間外の小さい棒1本 */
    public static class MonthBar {
        private final String label;
        private final String value;
        private final int heightPercent;
        private final boolean current;
        private final boolean overLimit;
        private final String legalValue;
        private final int legalHeightPercent;

        MonthBar(String label, String value, int heightPercent, boolean current, boolean overLimit,
                 String legalValue, int legalHeightPercent) {
            this.label = label;
            this.value = value;
            this.heightPercent = heightPercent;
            this.current = current;
            this.overLimit = overLimit;
            this.legalValue = legalValue;
            this.legalHeightPercent = legalHeightPercent;
        }

        /** 法定休出（日曜の勤務）。時間外の棒の上に別の色で重ねる */
        public String getLegalValue() { return legalValue; }
        public int getLegalHeightPercent() { return legalHeightPercent; }
        public boolean isHasLegal() { return legalHeightPercent > 0 || !"0:00".equals(legalValue); }

        public String getLabel() { return label; }
        public String getValue() { return value; }
        public int getHeightPercent() { return heightPercent; }
        public boolean isCurrent() { return current; }
        public boolean isOverLimit() { return overLimit; }
    }

    private final Subordinate staff;
    private final Summary s;

    public SubordinateSummaryView(Subordinate staff, Summary summary) {
        this.staff = staff;
        this.s = summary;
    }

    public String getStaffId() { return staff.getStaffId(); }
    public String getName() { return staff.getName(); }
    public String getDepartmentName() { return staff.getDepartmentName(); }

    // ---- 時間外 ----
    /** 当月の時間外の見込み（実績＋予定） */
    public String getMonthOvertime() { return hm(s.monthOutlookMinutes()); }
    /** 当月の時間外の実績 */
    public String getMonthActual() { return hm(s.month().overtimeMinutes()); }
    /** 当月の予定（承認済み・申請中。まだ実績がない日の分） */
    public String getMonthPlannedApproved() { return hm(s.plannedApprovedMinutes()); }
    public String getMonthPlannedPending() { return hm(s.plannedPendingMinutes()); }
    public boolean isHasMonthPlan() { return s.plannedApprovedMinutes() + s.plannedPendingMinutes() > 0; }
    /** 積み上げの棒の長さ（実績・承認済みの予定・申請中の予定。合わせて100%まで） */
    public int getMonthActualBar() { return bar(s.month().overtimeMinutes(), 0); }
    public int getMonthApprovedBar() { return bar(s.plannedApprovedMinutes(), s.month().overtimeMinutes()); }
    public int getMonthPendingBar() {
        return bar(s.plannedPendingMinutes(), s.month().overtimeMinutes() + s.plannedApprovedMinutes());
    }
    public String getYearOvertime() { return hm(s.year().overtimeMinutes()); }
    public int getMonthOvertimePercent() { return s.monthOvertimePercent(); }
    public int getYearOvertimePercent() { return s.yearOvertimePercent(); }
    /** 棒の長さ（100%まで） */
    public int getMonthBarPercent() { return Math.min(100, s.monthOvertimePercent()); }
    public int getYearBarPercent() { return Math.min(100, s.yearOvertimePercent()); }
    /** ok（80%未満）/ warn（80%以上）/ over（上限以上） */
    public String getMonthLevel() { return level(s.monthOvertimePercent()); }
    public String getYearLevel() { return level(s.yearOvertimePercent()); }

    /** 1月〜表示する月の時間外と法定休出（高さは、45時間とその年の最大の月（時間外＋法定休出）の大きい方を100%にする） */
    public List<MonthBar> getMonthBars() {
        int[] byMonth = s.overtimeByMonth();
        int[] legal = s.legalHolidayByMonth();
        int max = barMax();
        List<MonthBar> bars = new ArrayList<>();
        for (int i = 0; i < byMonth.length; i++) {
            bars.add(new MonthBar((i + 1) + "月", hm(byMonth[i]), (int) Math.round(byMonth[i] * 100.0 / max),
                    i == byMonth.length - 1, byMonth[i] >= StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES,
                    hm(legal[i]), (int) Math.round(legal[i] * 100.0 / max)));
        }
        return bars;
    }

    private int barMax() {
        int[] byMonth = s.overtimeByMonth();
        int[] legal = s.legalHolidayByMonth();
        int max = StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES;
        for (int i = 0; i < byMonth.length; i++) max = Math.max(max, byMonth[i] + legal[i]);
        return max;
    }

    /** 45時間の線の高さ（%） */
    public int getMonthLimitLinePercent() {
        return (int) Math.round(StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES * 100.0 / barMax());
    }

    // ---- 法定休出（日曜の勤務）と、時間外＋法定休出（36協定の月100時間未満・2〜6か月平均80時間以内） ----
    public String getMonthLegal() { return hm(s.month().legalHolidayMinutes()); }
    public String getYearLegal() { return hm(s.year().legalHolidayMinutes()); }
    public boolean isHasLegal() { return s.year().legalHolidayMinutes() > 0; }
    /** 当月の時間外（見込み）＋法定休出 */
    public String getMonthCombined() { return hm(s.monthCombinedMinutes()); }
    public int getMonthCombinedPercent() { return s.monthCombinedPercent(); }
    /** 100時間未満なので、100時間ちょうどで超過 */
    public String getMonthCombinedLevel() { return level(s.monthCombinedPercent()); }
    /** 月100時間の棒：時間外（見込み）と法定休出を重ねる */
    public int getCombinedOvertimeBar() { return bar100(s.monthOutlookMinutes(), 0); }
    public int getCombinedLegalBar() { return bar100(s.month().legalHolidayMinutes(), s.monthOutlookMinutes()); }
    /** 2〜6か月の平均のうち一番高いもの */
    public String getAverage() { return hm(s.maxAverage().minutes()); }
    public int getAverageMonths() { return s.maxAverage().months(); }
    public int getAveragePercent() { return s.averagePercent(); }
    public int getAverageBarPercent() { return Math.min(100, s.averagePercent()); }
    /** 80時間「以内」なので、80時間を超えたら超過 */
    public String getAverageLevel() {
        int m = s.maxAverage().minutes();
        if (m > StaffWorkSummaryCalculator.COMBINED_AVERAGE_LIMIT_MINUTES) return "over";
        return m * 100 >= StaffWorkSummaryCalculator.COMBINED_AVERAGE_LIMIT_MINUTES * 80 ? "warn" : "ok";
    }

    private static int bar100(int minutes, int before) {
        int limit = StaffWorkSummaryCalculator.COMBINED_MONTHLY_LIMIT_MINUTES;
        int start = (int) Math.min(100, Math.round(before * 100.0 / limit));
        int end = (int) Math.min(100, Math.round((before + minutes) * 100.0 / limit));
        return Math.max(0, end - start);
    }

    // ---- 総勤務・出勤日数 ----
    public String getMonthTotal() { return hm(s.month().totalMinutes()); }
    public String getYearTotal() { return hm(s.year().totalMinutes()); }
    public int getMonthWorkDays() { return s.month().workDays(); }
    public int getYearWorkDays() { return s.year().workDays(); }
    public int getMonthHolidayWorkDays() { return s.month().holidayWorkDays(); }
    public int getYearHolidayWorkDays() { return s.year().holidayWorkDays(); }

    // ---- 有給 ----
    public String getMonthPaidLeave() { return days(s.month().paidLeaveDays()); }
    public String getYearPaidLeave() { return days(s.year().paidLeaveDays()); }
    public String getPaidLeaveRemaining() { return s.paidLeaveRemaining() == null ? "—" : days(s.paidLeaveRemaining()); }

    // ---- 残業事前申請（当月） ----
    public int getPendingRequests() { return s.pendingRequests(); }
    public int getApprovedRequests() { return s.approvedRequests(); }
    public int getUnappliedDays() { return s.unappliedDays(); }

    /** 上限に対する棒の長さ（%）。前の分（before）と合わせて100%を超えない */
    private static int bar(int minutes, int before) {
        int limit = StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES;
        int start = (int) Math.min(100, Math.round(before * 100.0 / limit));
        int end = (int) Math.min(100, Math.round((before + minutes) * 100.0 / limit));
        return Math.max(0, end - start);
    }

    static String hm(int minutes) {
        int m = Math.max(0, minutes);
        return (m / 60) + ":" + String.format("%02d", m % 60);
    }

    static String days(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static String level(int percent) {
        if (percent >= 100) return "over";
        if (percent >= 80) return "warn";
        return "ok";
    }
}
