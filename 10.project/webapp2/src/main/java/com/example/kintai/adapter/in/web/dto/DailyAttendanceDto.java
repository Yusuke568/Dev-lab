package com.example.kintai.adapter.in.web.dto;

import java.time.LocalDate;

/**
 * 日次勤怠惁E��を表現するDTO、E
 * プレゼンチE�Eション層�E�ビュー�E�での表示に特化したデータ構造です、E
 */
public class DailyAttendanceDto {

    private final LocalDate date;
    private final String startTime;
    private final String endTime;
    private final String workHours;
    private final String workDescription;
    private final Integer abstractId;
    private final Integer correctionId;
    private final Integer correctionUsTime;
    private final Integer correctionMidTime;
    private final int approvalStatus;
    private final int overtimeMinutes;

    public DailyAttendanceDto(LocalDate date, String startTime, String endTime, String workHours, String workDescription, Integer abstractId, Integer correctionId, Integer correctionUsTime, Integer correctionMidTime, int approvalStatus, int overtimeMinutes) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workHours = workHours;
        this.workDescription = workDescription;
        this.abstractId = abstractId;
        this.correctionId = correctionId;
        this.correctionUsTime = correctionUsTime;
        this.correctionMidTime = correctionMidTime;
        this.approvalStatus = approvalStatus;
        this.overtimeMinutes = overtimeMinutes;
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
    
    public Integer getAbstractId() { return abstractId; }
    public Integer getCorrectionId() { return correctionId; }
    public Integer getCorrectionUsTime() { return correctionUsTime; }
    public Integer getCorrectionMidTime() { return correctionMidTime; }
    public int getApprovalStatus() { return approvalStatus; }
    public int getOvertimeMinutes() { return overtimeMinutes; }

    public String getCorrectionUsTimeFormatted() { return formatMinutes(correctionUsTime); }
    public String getCorrectionMidTimeFormatted() { return formatMinutes(correctionMidTime); }
    public String getOvertimeMinutesFormatted() { return formatMinutes(overtimeMinutes); }

    private static String formatMinutes(Integer totalMinutes) {
        if (totalMinutes == null) {
            return "";
        }
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
