package com.example.kintai.domain.model.attendance;

import java.time.LocalDate;

/**
 * 承認履歴一覧の絞り込み条件。すべての項目は任意（未指定=絞り込みなし）。
 */
public class ApprovalHistoryFilter {

    private final String employeeId;
    private final String decision; // "APPROVED" / "REJECTED" / null=すべて
    private final LocalDate fromDate;
    private final LocalDate toDate;

    public ApprovalHistoryFilter(String employeeId, String decision, LocalDate fromDate, LocalDate toDate) {
        this.employeeId = blankToNull(employeeId);
        this.decision = blankToNull(decision);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public String getEmployeeId() { return employeeId; }
    public String getDecision() { return decision; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
}
