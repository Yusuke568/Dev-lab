package com.example.kintai.application.service;

import com.example.kintai.application.port.in.DecideOvertimeRequestUseCase;
import com.example.kintai.application.port.in.GetMyOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.SubmitOvertimeRequestUseCase;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeRequestPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 残業事前申請のユースケース実装。
 *
 * 申請中・承認済みの申請がすでにある日付への申請は拒否する。
 * 却下された申請に対する再申請は、同じレコードを更新して申請中に戻す。
 */
public class OvertimeRequestService implements SubmitOvertimeRequestUseCase, GetMyOvertimeRequestsUseCase,
        GetPendingOvertimeRequestsUseCase, DecideOvertimeRequestUseCase {

    private final OvertimeRequestPort overtimeRequestPort;

    public OvertimeRequestService(OvertimeRequestPort overtimeRequestPort) {
        this.overtimeRequestPort = overtimeRequestPort;
    }

    @Override
    public void submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason) {
        int plannedOvertimeMinutes = OvertimeRequest.calculatePlannedOvertimeMinutes(plannedStartTime, plannedEndTime);
        if (plannedOvertimeMinutes <= 0) {
            throw new IllegalArgumentException("時間外が発生していません。標準勤務時間（09:00〜18:00）より早い出勤時刻、または遅い退勤時刻を入力してください。");
        }
        EmployeeId id = new EmployeeId(employeeId);
        Optional<OvertimeRequest> existing = overtimeRequestPort.findByEmployeeAndDate(id, date);

        if (existing.isPresent()) {
            OvertimeRequest current = existing.get();
            if (!current.isRejected()) {
                throw new IllegalStateException(
                        date + " はすでに申請済みです（" + current.getStatusLabel() + "）。却下された場合のみ再申請できます。");
            }
            overtimeRequestPort.resubmit(current.getId(), plannedOvertimeMinutes, plannedStartTime, plannedEndTime, reason);
        } else {
            overtimeRequestPort.insert(id, date, plannedOvertimeMinutes, plannedStartTime, plannedEndTime, reason);
        }
    }

    @Override
    public List<OvertimeRequest> getMyRequests(String employeeId) {
        return overtimeRequestPort.findByEmployee(new EmployeeId(employeeId));
    }

    @Override
    public List<OvertimeRequest> getPending() {
        return overtimeRequestPort.findPending();
    }

    @Override
    public void decide(int requestId, boolean approve, String decidedBy) {
        overtimeRequestPort.decide(requestId, approve, decidedBy);
    }
}
