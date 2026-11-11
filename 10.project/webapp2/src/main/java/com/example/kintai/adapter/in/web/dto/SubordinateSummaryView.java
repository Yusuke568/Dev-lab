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

        MonthBar(String label, String value, int heightPercent, boolean current, boolean overLimit) {
            this.label = label;
            this.value = value;
            this.heightPercent = heightPercent;
            this.current = current;
            this.overLimit = overLimit;
        }

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
    public String getMonthOvertime() { return hm(s.month().overtimeMinutes()); }
    public String getYearOvertime() { return hm(s.year().overtimeMinutes()); }
    public int getMonthOvertimePercent() { return s.monthOvertimePercent(); }
    public int getYearOvertimePercent() { return s.yearOvertimePercent(); }
    /** 棒の長さ（100%まで） */
    public int getMonthBarPercent() { return Math.min(100, s.monthOvertimePercent()); }
    public int getYearBarPercent() { return Math.min(100, s.yearOvertimePercent()); }
    /** ok（80%未満）/ warn（80%以上）/ over（上限以上） */
    public String getMonthLevel() { return level(s.monthOvertimePercent()); }
    public String getYearLevel() { return level(s.yearOvertimePercent()); }

    /** 1月〜表示する月の時間外（高さは、45時間とその年の最大の月の大きい方を100%にする） */
    public List<MonthBar> getMonthBars() {
        int[] byMonth = s.overtimeByMonth();
        int max = StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES;
        for (int v : byMonth) max = Math.max(max, v);
        List<MonthBar> bars = new ArrayList<>();
        for (int i = 0; i < byMonth.length; i++) {
            bars.add(new MonthBar((i + 1) + "月", hm(byMonth[i]), (int) Math.round(byMonth[i] * 100.0 / max),
                    i == byMonth.length - 1, byMonth[i] >= StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES));
        }
        return bars;
    }

    /** 45時間の線の高さ（%） */
    public int getMonthLimitLinePercent() {
        int max = StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES;
        for (int v : s.overtimeByMonth()) max = Math.max(max, v);
        return (int) Math.round(StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES * 100.0 / max);
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
