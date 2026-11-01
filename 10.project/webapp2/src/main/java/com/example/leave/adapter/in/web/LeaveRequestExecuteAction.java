package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.LeaveRequestUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 休暇申請を承認・却下するアクション（承認者向け）。
 */
public class LeaveRequestExecuteAction implements Action {

    private final LeaveRequestUseCase leaveRequestUseCase;

    public LeaveRequestExecuteAction(LeaveRequestUseCase leaveRequestUseCase) {
        this.leaveRequestUseCase = leaveRequestUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            String decision = request.getParameter("decision");
            String decidedBy = (String) request.getSession().getAttribute("loginUser");

            leaveRequestUseCase.decide(requestId, "approve".equals(decision), decidedBy);
        } catch (Exception e) {
            request.getSession().setAttribute("approvalErrorMessage", e.getMessage());
        }

        return new View("/approvalList.do", true);
    }
}
