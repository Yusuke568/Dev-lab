package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.PendingApprovalItem;

import java.util.List;

/**
 * 承認者の配下の、承認待ちの勤怠一覧を取得するユースケース。
 */
public interface GetPendingApprovalsUseCase {

    List<PendingApprovalItem> getPendingApprovals(String approverId);
}
