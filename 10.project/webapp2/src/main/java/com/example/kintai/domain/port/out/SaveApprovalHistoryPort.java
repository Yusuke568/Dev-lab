package com.example.kintai.domain.port.out;

import java.time.LocalDate;

/**
 * 勤怠承認の決定を履歴として記録するためのポート。
 */
public interface SaveApprovalHistoryPort {

    /**
     * @param employeeId 対象社員ID
     * @param date       対象日
     * @param approved   true: 承認、false: 却下
     * @param decidedBy  決定を行った人物の社員ID
     */
    void record(String employeeId, LocalDate date, boolean approved, String decidedBy);
}
