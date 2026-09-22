package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.GetPendingApprovalsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.PendingApprovalItem;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 承認待ち勤怠（有給）一覧、および承認待ちの残業事前申請一覧を表示するアクション。
 */
public class ApprovalListAction implements Action {

    private final GetPendingApprovalsUseCase getPendingApprovalsUseCase;
    private final GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase;

    public ApprovalListAction(GetPendingApprovalsUseCase getPendingApprovalsUseCase,
                               GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase) {
        this.getPendingApprovalsUseCase = getPendingApprovalsUseCase;
        this.getPendingOvertimeRequestsUseCase = getPendingOvertimeRequestsUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<PendingApprovalItem> pendingItems = getPendingApprovalsUseCase.getPendingApprovals();
        List<OvertimeRequest> pendingOvertimeRequests = getPendingOvertimeRequestsUseCase.getPending();
        request.setAttribute("pendingItems", pendingItems);
        request.setAttribute("pendingOvertimeRequests", pendingOvertimeRequests);

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("approvalErrorMessage") != null) {
            request.setAttribute("errorMessage", session.getAttribute("approvalErrorMessage"));
            session.removeAttribute("approvalErrorMessage");
        }

        return new View("/WEB-INF/view/approval.jsp");
    }
}
