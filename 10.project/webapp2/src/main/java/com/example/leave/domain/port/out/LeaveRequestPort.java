package com.example.leave.domain.port.out;

import com.example.leave.domain.model.LeaveRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 休暇申請の永続化操作を定義するポート。
 */
public interface LeaveRequestPort {

    Optional<LeaveRequest> findById(int requestId);

    Optional<LeaveRequest> findByEmployeeAndDate(int staffId, LocalDate date);

    List<LeaveRequest> findByEmployee(int staffId);

    /** 指定した承認者の、いまの段で処理待ちになっている申請。 */
    List<LeaveRequest> findPendingForApprover(int approverId);

    /** 承認者が自分の段で承認したが、まだ申請中（後の段の承認待ち）の申請 */
    default List<LeaveRequest> findApprovedInProgressBy(int approverId) {
        return List.of();
    }

    /** 新規申請を保存し、採番された ID を返す。 */
    int insert(int staffId, LocalDate date, int abstractId, String reason);

    /** 却下された申請を、内容を変えて申請中・1段目に戻す。 */
    void resubmit(int requestId, int abstractId, String reason);

    /** どの部署の業務としての申請か（兼任者用。null なら主所属） */
    default void updateDepartment(int requestId, Integer departmentId) {
    }

    void updateStatus(int requestId, String status);

    void advanceLevel(int requestId);
}
