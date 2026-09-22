package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.ApprovalHistoryEntry;
import com.example.kintai.domain.model.attendance.ApprovalHistoryFilter;

import java.util.List;

/**
 * 勤怠承認の履歴を読み込むためのポート。
 */
public interface LoadApprovalHistoryPort {

    List<ApprovalHistoryEntry> loadAll(ApprovalHistoryFilter filter);
}
