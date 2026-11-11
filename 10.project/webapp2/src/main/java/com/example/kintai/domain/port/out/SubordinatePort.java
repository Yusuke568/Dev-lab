package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.Subordinate;

import java.util.List;

/**
 * 組織図上の配下の社員を調べるためのポート。
 */
public interface SubordinatePort {

    /** 社員が、承認者の配下（自分自身は除く）に所属しているか。 */
    boolean isManagedBy(String employeeId, String approverId);

    /** 承認者の配下の社員（自分自身は除く）を、部署の並び順で返す。 */
    List<Subordinate> findManagedStaff(String approverId);

    /**
     * 社員が所属する部署（主所属・兼任）と、その下の部署に所属する社員（本人を含む）。週報担当が作業報告書を取り込める範囲。
     */
    default List<Subordinate> findStaffInMyDepartments(String staffId) {
        return List.of();
    }

    // ---- 部下のサマリー用（読むだけ。社員ID → 行） ----

    /** 期間の勤怠（出勤・退勤・総勤務・時間外・勤務区分） */
    default java.util.Map<String, List<com.example.kintai.domain.service.StaffWorkSummaryCalculator.DayRow>> findAttendanceRows(
            List<String> staffIds, java.time.LocalDate from, java.time.LocalDate to) {
        return java.util.Map.of();
    }

    /** 期間の残業事前申請（対象日・状態） */
    default java.util.Map<String, List<com.example.kintai.domain.service.StaffWorkSummaryCalculator.RequestRow>> findOvertimeRequestRows(
            List<String> staffIds, java.time.LocalDate from, java.time.LocalDate to) {
        return java.util.Map.of();
    }

    /** 有給の残日数 */
    default java.util.Map<String, Double> findPaidLeaveRemaining(List<String> staffIds) {
        return java.util.Map.of();
    }
}
