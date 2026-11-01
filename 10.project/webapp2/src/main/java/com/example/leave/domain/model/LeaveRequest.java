package com.example.leave.domain.model;

import java.time.LocalDate;

/**
 * 休暇申請（有給・代休など）。承認ルートに沿って承認される。
 * 承認されても勤怠には自動反映しない（勤怠画面での入力は別に行う）。
 *
 * ステータス: PENDING（申請中）/ APPROVED（承認）/ REJECTED（却下）
 */
public class LeaveRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private final int id;
    private final String employeeId;
    private final String employeeName; // 承認一覧表示用
    private final LocalDate targetDate;
    private final int abstractId;
    private final String abstractName;
    private final String reason;
    private final String status;
    private final int currentLevel;
    private final String createdAt; // "yyyy-MM-dd HH:mm"

    public LeaveRequest(int id, String employeeId, String employeeName, LocalDate targetDate, int abstractId,
                        String abstractName, String reason, String status, int currentLevel, String createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.targetDate = targetDate;
        this.abstractId = abstractId;
        this.abstractName = abstractName;
        this.reason = reason;
        this.status = status;
        this.currentLevel = currentLevel;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getTargetDate() { return targetDate; }
    public int getAbstractId() { return abstractId; }
    public String getAbstractName() { return abstractName; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public int getCurrentLevel() { return currentLevel; }
    public String getCreatedAt() { return createdAt; }

    public boolean isPending() { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }

    /** 画面表示用の日本語ステータス。 */
    public String getStatusLabel() {
        if (isApproved()) return "承認済み";
        if (isRejected()) return "却下";
        return "申請中";
    }
}
