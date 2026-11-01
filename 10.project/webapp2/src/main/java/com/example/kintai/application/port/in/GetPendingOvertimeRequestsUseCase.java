package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.OvertimeRequest;

import java.util.List;

/**
 * 承認者宛ての、承認待ちの残業事前申請一覧を取得するユースケース。
 */
public interface GetPendingOvertimeRequestsUseCase {

    List<OvertimeRequest> getPending(String approverId);
}
