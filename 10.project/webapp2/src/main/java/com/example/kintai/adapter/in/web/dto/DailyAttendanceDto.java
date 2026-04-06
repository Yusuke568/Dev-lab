package com.example.kintai.adapter.in.web.dto;

import java.time.LocalDate;

/**
 * 日次勤怠情報を表現するDTO。
 * プレゼンテーション層（ビュー）での表示に特化したデータ構造です。
 */
public class DailyAttendanceDto {

    private final LocalDate date;
    private final String startTime;
    private final String endTime;
    private final String workHours;
    private final String workDescription;

    public DailyAttendanceDto(LocalDate date, String startTime, String endTime, String workHours, String workDescription) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workHours = workHours;
        this.workDescription = workDescription;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getWorkHours() {
        return workHours;
    }
    
    public String getWorkDescription() {
        return workDescription;
    }
}
