package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.PendingApprovalItem;

import java.util.List;

/**
 * 承認者の配下（組織図上で承認者になっている部署とその下位部署）の申請中勤怠を読み込むためのポート。
 */
public interface LoadPendingApprovalsPort {

    List<PendingApprovalItem> loadManagedBy(String approverId);

    /** 社員が、承認者の配下（自分自身は除く）に所属しているか。 */
    boolean isManagedBy(String employeeId, String approverId);
}
