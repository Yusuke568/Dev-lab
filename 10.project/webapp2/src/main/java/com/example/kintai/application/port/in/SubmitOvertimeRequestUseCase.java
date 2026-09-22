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
    void submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason);
}
