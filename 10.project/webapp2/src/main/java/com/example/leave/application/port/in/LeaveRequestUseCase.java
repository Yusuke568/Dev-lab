package com.example.leave.application.port.in;

import com.example.leave.domain.model.LeaveRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 休暇申請の申請・確認・承認を行うユースケース。
 */
public interface LeaveRequestUseCase {

    /** 休暇を申請する。却下済みの日付なら再申請として更新する。 */
    /** 主所属の部署の業務として申請する。 */
    default void submit(String employeeId, LocalDate date, int abstractId, String reason) {
        submit(employeeId, date, abstractId, reason, null);
    }

    /** @param departmentId どの部署の業務か（兼任している人が選ぶ）。null なら主所属 */
    void submit(String employeeId, LocalDate date, int abstractId, String reason, Integer departmentId);

    List<LeaveRequest> getMyRequests(String employeeId);

    /** 承認者宛ての、承認待ちの申請。 */
    List<LeaveRequest> getPending(String approverId);

    /** 自分は承認したが、まだ申請中（後の段の承認待ち）の申請 */
    default List<LeaveRequest> getApprovedInProgress(String approverId) {
        return List.of();
    }

    void decide(int requestId, boolean approve, String decidedBy);
}
