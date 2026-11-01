package com.example.kintai.adapter.in.web;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.RequestType;
import com.example.kintai.application.port.in.GetPendingApprovalsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.PendingApprovalItem;
import com.example.leave.application.port.in.LeaveRequestUseCase;
import com.example.leave.domain.model.LeaveRequest;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ログインした承認者宛ての承認待ちを表示するアクション。
 * 残業事前申請・休暇申請は承認ルート上で自分の段になっているもの、
 * 勤怠は組織図上の配下の社員のものだけを表示する。
 */
public class ApprovalListAction implements Action {

    private final GetPendingApprovalsUseCase getPendingApprovalsUseCase;
    private final GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase;
    private final LeaveRequestUseCase leaveRequestUseCase;
    private final ApprovalWorkflowService approvalWorkflowService;

    public ApprovalListAction(GetPendingApprovalsUseCase getPendingApprovalsUseCase,
                               GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase,
                               LeaveRequestUseCase leaveRequestUseCase,
                               ApprovalWorkflowService approvalWorkflowService) {
        this.getPendingApprovalsUseCase = getPendingApprovalsUseCase;
        this.getPendingOvertimeRequestsUseCase = getPendingOvertimeRequestsUseCase;
        this.leaveRequestUseCase = leaveRequestUseCase;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String approverId = (String) session.getAttribute("loginUser");

        List<PendingApprovalItem> pendingItems = getPendingApprovalsUseCase.getPendingApprovals(approverId);
        List<OvertimeRequest> pendingOvertimeRequests = getPendingOvertimeRequestsUseCase.getPending(approverId);
        List<LeaveRequest> pendingLeaveRequests = leaveRequestUseCase.getPending(approverId);

        request.setAttribute("pendingItems", pendingItems);
        request.setAttribute("pendingOvertimeRequests", pendingOvertimeRequests);
        request.setAttribute("pendingLeaveRequests", pendingLeaveRequests);
        request.setAttribute("overtimeRoutes", approvalWorkflowService.getRoutes(RequestType.OVERTIME,
                pendingOvertimeRequests.stream().map(OvertimeRequest::getId).collect(Collectors.toList())));
        request.setAttribute("leaveRoutes", approvalWorkflowService.getRoutes(RequestType.LEAVE,
                pendingLeaveRequests.stream().map(LeaveRequest::getId).collect(Collectors.toList())));

        if (session.getAttribute("approvalErrorMessage") != null) {
            request.setAttribute("errorMessage", session.getAttribute("approvalErrorMessage"));
            session.removeAttribute("approvalErrorMessage");
        }

        return new View("/WEB-INF/view/approval.jsp");
    }
}
