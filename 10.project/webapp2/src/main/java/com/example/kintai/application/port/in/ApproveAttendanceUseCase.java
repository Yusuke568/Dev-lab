package com.example.kintai.application.port.in;

import java.time.LocalDate;

/**
 * 申請中の勤怠記録を承認・却下するユースケース。
 */
public interface ApproveAttendanceUseCase {

    /**
     * @param employeeId 対象社員ID
     * @param date       対象日
     * @param approve    true: 承認、false: 却下
     * @param decidedBy  決定を行った人物（承認者）の社員ID
     */
    void decide(String employeeId, LocalDate date, boolean approve, String decidedBy);
}
