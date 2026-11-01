package com.example.leave.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.RequestType;
import com.example.leave.application.port.in.LeaveRequestUseCase;
import com.example.leave.domain.model.LeaveRequest;
import com.example.leave.domain.port.out.LeaveRequestPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 休暇申請のユースケース実装。
 *
 * 1日につき1件。申請中・承認済みの日付への申請は拒否し、却下された日付は再申請として更新する。
 * 承認は組織図に基づく承認ルート（ApprovalWorkflowService）に沿って1段ずつ進む。
 */
public class LeaveRequestService implements LeaveRequestUseCase {

    private final LeaveRequestPort leaveRequestPort;
    private final ApprovalWorkflowService approvalWorkflowService;

    public LeaveRequestService(LeaveRequestPort leaveRequestPort, ApprovalWorkflowService approvalWorkflowService) {
        this.leaveRequestPort = leaveRequestPort;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    public void submit(String employeeId, LocalDate date, int abstractId, String reason) {
        int staffId = Integer.parseInt(employeeId);
        Optional<LeaveRequest> existing = leaveRequestPort.findByEmployeeAndDate(staffId, date);
        if (existing.isPresent() && !existing.get().isRejected()) {
            throw new IllegalStateException(
                    date + " はすでに申請済みです（" + existing.get().getStatusLabel() + "）。却下された場合のみ再申請できます。");
        }
        approvalWorkflowService.checkRouteAvailable(RequestType.LEAVE, employeeId);

        int requestId;
        if (existing.isPresent()) {
            requestId = existing.get().getId();
            leaveRequestPort.resubmit(requestId, abstractId, reason);
        } else {
            requestId = leaveRequestPort.insert(staffId, date, abstractId, reason);
        }
        approvalWorkflowService.startRoute(RequestType.LEAVE, requestId, employeeId);
    }

    @Override
    public List<LeaveRequest> getMyRequests(String employeeId) {
        return leaveRequestPort.findByEmployee(Integer.parseInt(employeeId));
    }

    @Override
    public List<LeaveRequest> getPending(String approverId) {
        return leaveRequestPort.findPendingForApprover(Integer.parseInt(approverId));
    }

    @Override
    public void decide(int requestId, boolean approve, String decidedBy) {
        LeaveRequest request = leaveRequestPort.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("対象の休暇申請が見つかりません: " + requestId));
        if (!request.isPending()) {
            throw new IllegalStateException("申請中の休暇申請のみ承認・却下できます。");
        }

        ApprovalOutcome outcome = approvalWorkflowService.decide(
                RequestType.LEAVE, requestId, request.getCurrentLevel(), decidedBy, approve);
        switch (outcome) {
            case NEXT_LEVEL:
                leaveRequestPort.advanceLevel(requestId);
                break;
            case APPROVED:
                leaveRequestPort.updateStatus(requestId, LeaveRequest.STATUS_APPROVED);
                break;
            case REJECTED:
                leaveRequestPort.updateStatus(requestId, LeaveRequest.STATUS_REJECTED);
                break;
        }
    }
}
