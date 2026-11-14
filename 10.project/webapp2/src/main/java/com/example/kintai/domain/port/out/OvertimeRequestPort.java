package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 残業事前申請の永続化操作を定義するポート。
 */
public interface OvertimeRequestPort {

    Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId employeeId, LocalDate date);

    List<OvertimeRequest> findByEmployee(EmployeeId employeeId);

    Optional<OvertimeRequest> findById(int requestId);

    /** 指定した承認者の、いまの段で処理待ちになっている申請。 */
    List<OvertimeRequest> findPendingForApprover(String approverId);

    /** 承認者が自分の段で承認したが、まだ申請中（後の段の承認待ち）の申請 */
    default List<OvertimeRequest> findApprovedInProgressBy(String approverId) {
        return List.of();
    }

    /** 新規申請を保存し、採番された ID を返す。 */
    int insert(EmployeeId employeeId, LocalDate date, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason);

    void resubmit(int requestId, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason);

    /** 最終的な承認・却下を記録する。 */
    void decide(int requestId, boolean approve, String decidedBy);

    /** 次の段の承認待ちに進める。 */
    void advanceLevel(int requestId);

    /**
     * 申請中の申請を取り下げにする。
     * @return 取り下げにできたら true（もう承認・却下されていたら false）
     */
    default boolean withdraw(int requestId) {
        throw new UnsupportedOperationException("withdraw");
    }

    /** どの部署の業務としての申請か（兼任者用。null なら主所属） */
    /** 申請と、その承認ルートの段（時間外申請書の出力用） */
    record FormRequest(OvertimeRequest request,
                       List<com.example.kintai.domain.service.OvertimeFormCalculator.Step> steps) {
    }

    /** 社員の期間の、申請中・承認済みの申請（対象日の順）と承認ルートの段 */
    default List<FormRequest> findForForm(String staffId, LocalDate from, LocalDate to) {
        return List.of();
    }

    /** 承認ルートのうち条件つきの段を除いた段数を入れる（申請したとき） */
    default void updateBaseLevels(int requestId, int baseLevels) {
    }

    /** 実績の確認日を入れる（null で消す） */
    default void updateActualConfirmedOn(int requestId, LocalDate date) {
    }

    /** 補正（通常・深夜）を入れる */
    default void updateCorrection(int requestId, int correctionMinutes, int correctionMidMinutes) {
    }

    /** PJ名と摘要を入れる（時間外申請書の出力用） */
    default void updateDetails(int requestId, String projectName, String category) {
    }

    default void updateDepartment(int requestId, Integer departmentId) {
    }
}
