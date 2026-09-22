package com.example.kintai.adapter.in.web.dto;

import java.time.YearMonth;
import java.util.List;

/**
 * 月次勤怠情報を表現するDTO。
 * 日次DTO (DailyAttendanceDto) のリストと、月全体のサマリー情報を保持する。
 */
public class MonthlyAttendanceDto {

    // 36協定における時間外労働の一般的な上限（原則）。特別条項は考慮しない。
    private static final long MONTHLY_OVERTIME_LIMIT_MINUTES = 45L * 60;
    private static final long ANNUAL_OVERTIME_LIMIT_MINUTES = 540L * 60;

    private final YearMonth yearMonth;
    private final List<DailyAttendanceDto> dailyRecords;
    private final String totalWorkHours;
    private final long totalWorkMinutes;
    private final long totalOvertimeMinutes;
    private final long annualOvertimeMinutes;
    private final String employeeName;

    public MonthlyAttendanceDto(YearMonth yearMonth, List<DailyAttendanceDto> dailyRecords, String totalWorkHours,
                                 long totalWorkMinutes, long totalOvertimeMinutes, long annualOvertimeMinutes,
                                 String employeeName) {
        this.yearMonth = yearMonth;
        this.dailyRecords = dailyRecords;
        this.totalWorkHours = totalWorkHours;
        this.totalWorkMinutes = totalWorkMinutes;
        this.totalOvertimeMinutes = totalOvertimeMinutes;
        this.annualOvertimeMinutes = annualOvertimeMinutes;
        this.employeeName = employeeName;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public List<DailyAttendanceDto> getDailyRecords() {
        return dailyRecords;
    }

    public String getTotalWorkHours() {
        return totalWorkHours;
    }

    public long getTotalWorkMinutes() {
        return totalWorkMinutes;
    }

    public long getTotalOvertimeMinutes() {
        return totalOvertimeMinutes;
    }

    public long getAnnualOvertimeMinutes() {
        return annualOvertimeMinutes;
    }

    public String getTotalOvertimeFormatted() {
        return formatMinutes(totalOvertimeMinutes);
    }

    public String getAnnualOvertimeFormatted() {
        return formatMinutes(annualOvertimeMinutes);
    }

    /** 当月の時間外が原則の上限（45時間）以内かどうか。 */
    public boolean isMonthlyOvertimeWithinLimit() {
        return totalOvertimeMinutes <= MONTHLY_OVERTIME_LIMIT_MINUTES;
    }

    /** 年間の時間外が原則の上限（540時間）以内かどうか。 */
    public boolean isAnnualOvertimeWithinLimit() {
        return annualOvertimeMinutes <= ANNUAL_OVERTIME_LIMIT_MINUTES;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    private static String formatMinutes(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }
}
