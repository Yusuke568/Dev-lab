package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.OvertimeRequest;
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
    default boolean submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason,
                           Integer departmentId) {
        return submit(employeeId, date, plannedStartTime, plannedEndTime, reason, departmentId, null, null);
    }

    /**
     * @param projectName PJ名（申請画面からは必須。null なら入れない）
     * @param category    摘要（OvertimeRequest.CATEGORIES のどれか。null なら入れない）
     */
    default boolean submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason,
                           Integer departmentId, String projectName, String category) {
        return submit(employeeId, date, plannedStartTime, plannedEndTime, reason, departmentId, projectName, category,
                null, null);
    }

    /**
     * @param correctionMinutes    補正（通常）の分。null ならその日の勤怠の補正（勤怠がなければ0）
     * @param correctionMidMinutes 補正（深夜）の分。null ならその日の勤怠の補正（勤怠がなければ0）
     */
    boolean submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason,
                   Integer departmentId, String projectName, String category,
                   Integer correctionMinutes, Integer correctionMidMinutes);

    /**
     * 自分の申請中の申請を取り下げる（承認・却下される前だけ）。取り下げた日は、もう一度申請できる。
     * @return 取り下げた申請（画面のメッセージ用）
     * @throws IllegalArgumentException 申請が見つからない・自分の申請でない
     * @throws IllegalStateException    もう承認・却下・取り下げされている
     */
    default OvertimeRequest withdraw(String employeeId, int requestId) {
        throw new UnsupportedOperationException("withdraw");
    }
}
