package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.OvertimeRequest;

import java.util.List;

/**
 * 自分（ログイン社員）の残業事前申請一覧を取得するユースケース。
 */
public interface GetMyOvertimeRequestsUseCase {

    List<OvertimeRequest> getMyRequests(String employeeId);
}
