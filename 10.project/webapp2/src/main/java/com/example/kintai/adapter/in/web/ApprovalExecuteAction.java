package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.ApproveAttendanceUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 勤怠の承認・却下を実行するアクション。
 */
public class ApprovalExecuteAction implements Action {

    private final ApproveAttendanceUseCase approveAttendanceUseCase;

    public ApprovalExecuteAction(ApproveAttendanceUseCase approveAttendanceUseCase) {
        this.approveAttendanceUseCase = approveAttendanceUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String employeeId = request.getParameter("employeeId");
        String dateParam = request.getParameter("date");
        String decision = request.getParameter("decision"); // "approve" or "reject"

        try {
            LocalDate date = LocalDate.parse(dateParam, DateTimeFormatter.ISO_LOCAL_DATE);
            String decidedBy = (String) request.getSession().getAttribute("loginUser");
            approveAttendanceUseCase.decide(employeeId, date, "approve".equals(decision), decidedBy);
        } catch (Exception e) {
            request.getSession().setAttribute("approvalErrorMessage", e.getMessage());
        }

        return new View("/approvalList.do", true);
    }
}
