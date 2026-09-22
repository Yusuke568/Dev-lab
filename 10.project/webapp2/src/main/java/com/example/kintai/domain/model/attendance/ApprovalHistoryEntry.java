package com.example.kintai.domain.model.attendance;

/**
 * 勤怠承認の履歴（監査ログ）を表す読み取り専用モデル。
 */
public class ApprovalHistoryEntry {
    private final String employeeId;
    private final String employeeName;
    private final String date;
    private final String decision; // "承認" or "却下"
    private final String decidedById;
    private final String decidedByName;
    private final String decidedAt; // yyyy-MM-dd HH:mm

    public ApprovalHistoryEntry(String employeeId, String employeeName, String date, String decision,
                                 String decidedById, String decidedByName, String decidedAt) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.decision = decision;
        this.decidedById = decidedById;
        this.decidedByName = decidedByName;
        this.decidedAt = decidedAt;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDate() { return date; }
    public String getDecision() { return decision; }
    public String getDecidedById() { return decidedById; }
    public String getDecidedByName() { return decidedByName; }
    public String getDecidedAt() { return decidedAt; }
}
