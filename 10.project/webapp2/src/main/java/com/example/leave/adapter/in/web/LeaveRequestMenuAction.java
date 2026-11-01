package com.example.leave.adapter.in.web;

import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.RequestType;
import com.example.entity.WorkType;
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
 * 休暇申請の画面（新規申請フォームと自分の申請履歴）を表示するアクション。
 */
public class LeaveRequestMenuAction implements Action {

    private final LeaveRequestUseCase leaveRequestUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;
    private final ApprovalWorkflowService approvalWorkflowService;

    public LeaveRequestMenuAction(LeaveRequestUseCase leaveRequestUseCase, GetWorkTypesUseCase getWorkTypesUseCase,
                                  ApprovalWorkflowService approvalWorkflowService) {
        this.leaveRequestUseCase = leaveRequestUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = (String) session.getAttribute("loginUser");

        List<LeaveRequest> myRequests = leaveRequestUseCase.getMyRequests(employeeId);
        request.setAttribute("myRequests", myRequests);
        request.setAttribute("routes", approvalWorkflowService.getRoutes(RequestType.LEAVE,
                myRequests.stream().map(LeaveRequest::getId).collect(Collectors.toList())));

        // 申請できる区分は、労働を伴わないもの（有給・代休・特休など）
        List<WorkType> leaveTypes = getWorkTypesUseCase.getWorkTypes().stream()
                .filter(w -> !w.isWork())
                .collect(Collectors.toList());
        request.setAttribute("leaveTypes", leaveTypes);

        if (session.getAttribute("leaveRequestErrorMessage") != null) {
            request.setAttribute("errorMessage", session.getAttribute("leaveRequestErrorMessage"));
            session.removeAttribute("leaveRequestErrorMessage");
        }

        return new View("/WEB-INF/view/leave_request_menu.jsp");
    }
}
