package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.ApprovalHistoryEntry;
import com.example.kintai.domain.model.attendance.ApprovalHistoryFilter;

import java.util.List;

/**
 * 勤怠承認の履歴を読み込むためのポート。
 */
public interface LoadApprovalHistoryPort {

    List<ApprovalHistoryEntry> loadAll(ApprovalHistoryFilter filter);

    /** 社員の期間内の日ごとの、いちばん新しい承認・却下の理由（理由のある日だけ） */
    default java.util.Map<java.time.LocalDate, String> findLatestComments(String employeeId,
                                                                        java.time.LocalDate from, java.time.LocalDate to) {
        return java.util.Map.of();
    }
}
