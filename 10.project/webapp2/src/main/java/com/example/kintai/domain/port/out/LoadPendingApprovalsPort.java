package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.PendingApprovalItem;

import java.util.List;

/**
 * 全社員の申請中（承認待ち）勤怠を横断的に読み込むためのポート。
 */
public interface LoadPendingApprovalsPort {

    List<PendingApprovalItem> loadAll();
}
