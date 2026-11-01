package com.example.kintai.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.RequestType;
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
 * 承認は組織図に基づく承認ルート（ApprovalWorkflowService）に沿って1段ずつ進む。
 */
public class OvertimeRequestService implements SubmitOvertimeRequestUseCase, GetMyOvertimeRequestsUseCase,
        GetPendingOvertimeRequestsUseCase, DecideOvertimeRequestUseCase {

    private final OvertimeRequestPort overtimeRequestPort;
    private final ApprovalWorkflowService approvalWorkflowService;

    public OvertimeRequestService(OvertimeRequestPort overtimeRequestPort, ApprovalWorkflowService approvalWorkflowService) {
        this.overtimeRequestPort = overtimeRequestPort;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    public void submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason) {
        int plannedOvertimeMinutes = OvertimeRequest.calculatePlannedOvertimeMinutes(plannedStartTime, plannedEndTime);
        if (plannedOvertimeMinutes <= 0) {
            throw new IllegalArgumentException("時間外が発生していません。標準勤務時間（09:00〜18:00）より早い出勤時刻、または遅い退勤時刻を入力してください。");
        }
        EmployeeId id = new EmployeeId(employeeId);
        Optional<OvertimeRequest> existing = overtimeRequestPort.findByEmployeeAndDate(id, date);

        if (existing.isPresent() && !existing.get().isRejected()) {
            throw new IllegalStateException(
                    date + " はすでに申請済みです（" + existing.get().getStatusLabel() + "）。却下された場合のみ再申請できます。");
        }
        // 承認者がいないまま申請だけ残らないよう、保存前にルートが組めるか確かめる
        approvalWorkflowService.checkRouteAvailable(RequestType.OVERTIME, employeeId);

        int requestId;
        if (existing.isPresent()) {
            requestId = existing.get().getId();
            overtimeRequestPort.resubmit(requestId, plannedOvertimeMinutes, plannedStartTime, plannedEndTime, reason);
        } else {
            requestId = overtimeRequestPort.insert(id, date, plannedOvertimeMinutes, plannedStartTime, plannedEndTime, reason);
        }
        approvalWorkflowService.startRoute(RequestType.OVERTIME, requestId, employeeId);
    }

    @Override
    public List<OvertimeRequest> getMyRequests(String employeeId) {
        return overtimeRequestPort.findByEmployee(new EmployeeId(employeeId));
    }

    @Override
    public List<OvertimeRequest> getPending(String approverId) {
        return overtimeRequestPort.findPendingForApprover(approverId);
    }

    @Override
    public void decide(int requestId, boolean approve, String decidedBy) {
        OvertimeRequest request = overtimeRequestPort.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("対象の残業事前申請が見つかりません: " + requestId));
        if (!request.isPending()) {
            throw new IllegalStateException("申請中の残業事前申請のみ承認・却下できます。");
        }

        ApprovalOutcome outcome = approvalWorkflowService.decide(
                RequestType.OVERTIME, requestId, request.getCurrentLevel(), decidedBy, approve);
        switch (outcome) {
            case NEXT_LEVEL:
                overtimeRequestPort.advanceLevel(requestId);
                break;
            case APPROVED:
                overtimeRequestPort.decide(requestId, true, decidedBy);
                break;
            case REJECTED:
                overtimeRequestPort.decide(requestId, false, decidedBy);
                break;
        }
    }
}
