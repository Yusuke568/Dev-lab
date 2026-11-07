package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.LeaveRequestUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

/**
 * 休暇申請の新規提出・再提出を行うアクション。
 */
public class LeaveRequestSubmitAction implements Action {

    private final LeaveRequestUseCase leaveRequestUseCase;

    public LeaveRequestSubmitAction(LeaveRequestUseCase leaveRequestUseCase) {
        this.leaveRequestUseCase = leaveRequestUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = (String) session.getAttribute("loginUser");

        try {
            LocalDate date = LocalDate.parse(request.getParameter("targetDate"));
            int abstractId = Integer.parseInt(request.getParameter("abstractId"));
            String departmentParam = request.getParameter("departmentId");
            Integer departmentId = departmentParam == null || departmentParam.isBlank() ? null : Integer.valueOf(departmentParam);
            leaveRequestUseCase.submit(employeeId, date, abstractId, request.getParameter("reason"), departmentId);
        } catch (Exception e) {
            session.setAttribute("leaveRequestErrorMessage", e.getMessage());
        }

        return new View("/leaveRequestMenu.do", true);
    }
}
