package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.DecideOvertimeRequestUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 残業事前申請を承認・却下するアクション（管理者向け）。
 */
public class OvertimeRequestExecuteAction implements Action {

    private final DecideOvertimeRequestUseCase decideOvertimeRequestUseCase;

    public OvertimeRequestExecuteAction(DecideOvertimeRequestUseCase decideOvertimeRequestUseCase) {
        this.decideOvertimeRequestUseCase = decideOvertimeRequestUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            String decision = request.getParameter("decision");
            String decidedBy = (String) request.getSession().getAttribute("loginUser");

            decideOvertimeRequestUseCase.decide(requestId, "approve".equals(decision), decidedBy);
        } catch (Exception e) {
            request.getSession().setAttribute("approvalErrorMessage", e.getMessage());
        }

        return new View("/approvalList.do", true);
    }
}
