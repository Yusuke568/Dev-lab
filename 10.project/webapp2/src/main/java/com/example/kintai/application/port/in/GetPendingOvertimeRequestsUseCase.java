package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.OvertimeRequest;

import java.util.List;

/**
 * 承認待ちの残業事前申請一覧を取得するユースケース（管理者向け）。
 */
public interface GetPendingOvertimeRequestsUseCase {

    List<OvertimeRequest> getPending();
}
