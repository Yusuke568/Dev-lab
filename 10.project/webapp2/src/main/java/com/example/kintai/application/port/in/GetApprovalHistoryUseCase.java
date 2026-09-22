package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.ApprovalHistoryEntry;
import com.example.kintai.domain.model.attendance.ApprovalHistoryFilter;

import java.util.List;

/**
 * 勤怠承認の履歴一覧を取得するユースケース。
 */
public interface GetApprovalHistoryUseCase {

    List<ApprovalHistoryEntry> getHistory(ApprovalHistoryFilter filter);
}
