package com.example.leave.application.port.in;

import com.example.leave.domain.model.LeaveRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 休暇申請の申請・確認・承認を行うユースケース。
 */
public interface LeaveRequestUseCase {

    /** 休暇を申請する。却下済みの日付なら再申請として更新する。 */
    void submit(String employeeId, LocalDate date, int abstractId, String reason);

    List<LeaveRequest> getMyRequests(String employeeId);

    /** 承認者宛ての、承認待ちの申請。 */
    List<LeaveRequest> getPending(String approverId);

    void decide(int requestId, boolean approve, String decidedBy);
}
