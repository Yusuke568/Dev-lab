package com.example.kintai.adapter.in.web.dto;

import java.time.YearMonth;
import java.util.List;

/**
 * 月次勤怠情報を表現するDTO。
 * DTO (DailyAttendanceDto) のリストと、月全体のサマリー情報を保持します。
 */
public class MonthlyAttendanceDto {
    private final YearMonth yearMonth;
    private final List<DailyAttendanceDto> dailyRecords;
    private final String totalWorkHours;
    private final String employeeName;

    public MonthlyAttendanceDto(YearMonth yearMonth, List<DailyAttendanceDto> dailyRecords, String totalWorkHours, String employeeName) {
        this.yearMonth = yearMonth;
        this.dailyRecords = dailyRecords;
        this.totalWorkHours = totalWorkHours;
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

    public String getEmployeeName() {
        return employeeName;
    }
}
