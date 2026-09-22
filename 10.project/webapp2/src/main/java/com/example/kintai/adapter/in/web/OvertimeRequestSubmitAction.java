package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.SubmitOvertimeRequestUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 残業事前申請の新規提出・再提出を行うアクション。
 */
public class OvertimeRequestSubmitAction implements Action {

    private final SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase;

    public OvertimeRequestSubmitAction(SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase) {
        this.submitOvertimeRequestUseCase = submitOvertimeRequestUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;

        try {
            if (employeeId == null) {
                throw new IllegalStateException("ログインしてください。");
            }
            LocalDate date = LocalDate.parse(request.getParameter("targetDate"), DateTimeFormatter.ISO_LOCAL_DATE);
            String plannedStartTime = request.getParameter("plannedStartTime");
            String plannedEndTime = request.getParameter("plannedEndTime");
            String reason = request.getParameter("reason");

            submitOvertimeRequestUseCase.submit(employeeId, date, plannedStartTime, plannedEndTime, reason);
        } catch (Exception e) {
            if (session != null) {
                session.setAttribute("overtimeRequestErrorMessage", e.getMessage());
            }
        }

        return new View("/overtimeRequestMenu.do", true);
    }
}
