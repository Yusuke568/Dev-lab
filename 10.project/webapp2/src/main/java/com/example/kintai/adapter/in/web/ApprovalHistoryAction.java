package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.GetApprovalHistoryUseCase;
import com.example.kintai.domain.model.attendance.ApprovalHistoryEntry;
import com.example.kintai.domain.model.attendance.ApprovalHistoryFilter;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 勤怠承認の履歴一覧を表示するアクション。クエリパラメータで絞り込みが可能。
 */
public class ApprovalHistoryAction implements Action {

    private final GetApprovalHistoryUseCase getApprovalHistoryUseCase;

    public ApprovalHistoryAction(GetApprovalHistoryUseCase getApprovalHistoryUseCase) {
        this.getApprovalHistoryUseCase = getApprovalHistoryUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String employeeId = request.getParameter("employeeId");
        String decision = request.getParameter("decision");
        String fromParam = request.getParameter("from");
        String toParam = request.getParameter("to");

        LocalDate from = (fromParam != null && !fromParam.isBlank()) ? LocalDate.parse(fromParam) : null;
        LocalDate to = (toParam != null && !toParam.isBlank()) ? LocalDate.parse(toParam) : null;

        ApprovalHistoryFilter filter = new ApprovalHistoryFilter(employeeId, decision, from, to);
        List<ApprovalHistoryEntry> historyItems = getApprovalHistoryUseCase.getHistory(filter);

        request.setAttribute("historyItems", historyItems);
        request.setAttribute("filterEmployeeId", employeeId);
        request.setAttribute("filterDecision", decision);
        request.setAttribute("filterFrom", fromParam);
        request.setAttribute("filterTo", toParam);

        return new View("/WEB-INF/view/approval_history.jsp");
    }
}
