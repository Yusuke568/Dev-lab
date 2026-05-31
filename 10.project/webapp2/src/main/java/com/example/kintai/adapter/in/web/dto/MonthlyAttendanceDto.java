package com.example.kintai.adapter.in.web.dto;

import java.time.YearMonth;
import java.util.List;

/**
 * 月次勤怠惁E��を表現するDTO、E
 * DTO (DailyAttendanceDto) のリストと、月全体�Eサマリー惁E��を保持します、E
 */
public class MonthlyAttendanceDto {
    private final YearMonth yearMonth;
    private final List<DailyAttendanceDto> dailyRecords;
    private final String totalWorkHours;
    private final long totalWorkMinutes;
    private final String employeeName;

    public MonthlyAttendanceDto(YearMonth yearMonth, List<DailyAttendanceDto> dailyRecords, String totalWorkHours, long totalWorkMinutes, String employeeName) {
        this.yearMonth = yearMonth;
        this.dailyRecords = dailyRecords;
        this.totalWorkHours = totalWorkHours;
        this.totalWorkMinutes = totalWorkMinutes;
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

    public String getEmployeeName() {
        return employeeName;
    }
}
