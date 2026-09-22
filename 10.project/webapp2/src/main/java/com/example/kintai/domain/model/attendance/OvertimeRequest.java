package com.example.kintai.domain.model.attendance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 残業の事前申請。時間外労働を行う前に申請し、承認された場合のみ
 * 勤怠保存時の追加承認（時間外による承認待ち化）をスキップできる。
 *
 * 標準勤務時間（09:00〜18:00）より早い予定出勤・遅い予定退勤を、それぞれ
 * 「早出」「残業」として別々に把握できるよう、予定出勤時刻・予定退勤時刻の
 * 両方を保持する（前倒し出勤と残業が同日に両方発生するケースにも対応）。
 *
 * ステータス: PENDING（申請中）/ APPROVED（承認）/ REJECTED（却下）
 */
public class OvertimeRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private static final LocalTime STANDARD_START = LocalTime.of(9, 0);
    private static final LocalTime STANDARD_END = LocalTime.of(18, 0);

    private final int id;
    private final String employeeId;
    private final String employeeName; // 承認一覧表示用。個人の申請一覧では空でもよい
    private final LocalDate targetDate;
    private final int plannedOvertimeMinutes; // 早出+残業の合計（分）
    private final String plannedStartTime; // "HH:mm" or null
    private final String plannedEndTime; // "HH:mm" or null
    private final String reason;
    private final String status;
    private final String decidedBy;
    private final String decidedAt; // "yyyy-MM-dd HH:mm" or null
    private final String createdAt; // "yyyy-MM-dd HH:mm"

    public OvertimeRequest(int id, String employeeId, String employeeName, LocalDate targetDate,
                            int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason,
                            String status, String decidedBy, String decidedAt, String createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.targetDate = targetDate;
        this.plannedOvertimeMinutes = plannedOvertimeMinutes;
        this.plannedStartTime = plannedStartTime;
        this.plannedEndTime = plannedEndTime;
        this.reason = reason;
        this.status = status;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getTargetDate() { return targetDate; }
    public int getPlannedOvertimeMinutes() { return plannedOvertimeMinutes; }
    public String getPlannedStartTime() { return plannedStartTime; }
    public String getPlannedEndTime() { return plannedEndTime; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getDecidedBy() { return decidedBy; }
    public String getDecidedAt() { return decidedAt; }
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

    public String getPlannedOvertimeFormatted() {
        return formatMinutes(plannedOvertimeMinutes);
    }

    /** 予定出勤時刻が標準（09:00）より早い場合の早出時間（分）。早出でなければ0。 */
    public int getPlannedEarlyMinutes() {
        return calculateEarlyMinutes(plannedStartTime);
    }

    /** 予定退勤時刻が標準（18:00）より遅い場合の残業時間（分）。残業でなければ0。 */
    public int getPlannedLateMinutes() {
        return calculateLateMinutes(plannedEndTime);
    }

    public String getPlannedEarlyMinutesFormatted() { return formatMinutes(getPlannedEarlyMinutes()); }
    public String getPlannedLateMinutesFormatted() { return formatMinutes(getPlannedLateMinutes()); }

    /** 早出・残業を「早出00:30 / 残業02:00」のように内訳表示するための文字列。 */
    public String getPlannedOvertimeBreakdown() {
        int early = getPlannedEarlyMinutes();
        int late = getPlannedLateMinutes();
        List<String> parts = new ArrayList<>();
        if (early > 0) parts.add("早出" + formatMinutes(early));
        if (late > 0) parts.add("残業" + formatMinutes(late));
        return parts.isEmpty() ? formatMinutes(plannedOvertimeMinutes) : String.join(" / ", parts);
    }

    /** 予定出勤・退勤時刻（"HH:mm"、未入力可）から、早出+残業の合計時間外分数を計算する。 */
    public static int calculatePlannedOvertimeMinutes(String plannedStartTime, String plannedEndTime) {
        return calculateEarlyMinutes(plannedStartTime) + calculateLateMinutes(plannedEndTime);
    }

    private static int calculateEarlyMinutes(String startTime) {
        if (startTime == null || startTime.isBlank()) return 0;
        LocalTime t = LocalTime.parse(startTime);
        return t.isBefore(STANDARD_START) ? (int) Duration.between(t, STANDARD_START).toMinutes() : 0;
    }

    private static int calculateLateMinutes(String endTime) {
        if (endTime == null || endTime.isBlank()) return 0;
        LocalTime t = LocalTime.parse(endTime);
        return t.isAfter(STANDARD_END) ? (int) Duration.between(STANDARD_END, t).toMinutes() : 0;
    }

    private static String formatMinutes(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
