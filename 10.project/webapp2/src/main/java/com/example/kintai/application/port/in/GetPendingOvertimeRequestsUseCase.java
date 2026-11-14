package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.OvertimeRequest;

import java.util.List;

/**
 * 承認者宛ての、承認待ちの残業事前申請一覧を取得するユースケース。
 */
public interface GetPendingOvertimeRequestsUseCase {

    List<OvertimeRequest> getPending(String approverId);

    /** 自分は承認したが、まだ申請中（後の段の承認待ち）の申請 */
    default List<OvertimeRequest> getApprovedInProgress(String approverId) {
        return List.of();
    }
}
