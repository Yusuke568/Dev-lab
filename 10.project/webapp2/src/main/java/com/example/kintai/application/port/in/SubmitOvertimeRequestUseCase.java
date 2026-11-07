package com.example.kintai.application.port.in;

import java.time.LocalDate;

/**
 * 残業の事前申請を提出する（新規申請・却下後の再申請の両方を扱う）ユースケース。
 */
public interface SubmitOvertimeRequestUseCase {

    /**
     * @param employeeId       申請者の社員ID
     * @param date             対象日
     * @param plannedStartTime 予定の出勤時刻（"HH:mm"）。標準（09:00）どおりならnull/空でよい
     * @param plannedEndTime   予定の退勤時刻（"HH:mm"）。標準（18:00）どおりならnull/空でよい
     * @param reason           申請理由
     */
    /** 主所属の部署の業務として申請する。 */
    default boolean submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason) {
        return submit(employeeId, date, plannedStartTime, plannedEndTime, reason, null);
    }

    /**
     * @param departmentId どの部署の業務か（兼任している人が選ぶ）。null なら主所属
     * @return 自動承認された（申請者が最上位の承認者で、ほかに承認者がいない）なら true
     */
    boolean submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason,
                   Integer departmentId);
}
