package com.example.kintai.domain.model.attendance;

/**
 * 承認待ち一覧画面用の読み取り専用モデル（複数社員をまたぐレポーティング用途）。
 */
public class PendingApprovalItem {
    private final String employeeId;
    private final String employeeName;
    private final String date; // yyyy-MM-dd
    private final String week;
    private final String startTime; // HH:mm or "-"
    private final String endTime;
    private final String abstractName;
    private final String memo;
    private final int overtimeMinutes;

    public PendingApprovalItem(String employeeId, String employeeName, String date, String week,
                                String startTime, String endTime, String abstractName, String memo,
                                int overtimeMinutes) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.week = week;
        this.startTime = startTime;
        this.endTime = endTime;
        this.abstractName = abstractName;
        this.memo = memo;
        this.overtimeMinutes = overtimeMinutes;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDate() { return date; }
    public String getWeek() { return week; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getAbstractName() { return abstractName; }
    public String getMemo() { return memo; }
    public int getOvertimeMinutes() { return overtimeMinutes; }
}
