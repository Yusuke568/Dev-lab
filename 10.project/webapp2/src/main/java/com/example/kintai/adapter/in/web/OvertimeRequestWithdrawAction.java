package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.SubmitOvertimeRequestUseCase;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 申請者が自分の残業事前申請を取り下げる（承認・却下される前だけ）。
 * 結果は残業事前申請の画面に1回だけ表示する。取り下げた日は、もう一度申請できる。
 */
public class OvertimeRequestWithdrawAction implements Action {

    private final SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase;

    public OvertimeRequestWithdrawAction(SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase) {
        this.submitOvertimeRequestUseCase = submitOvertimeRequestUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;
        if (employeeId == null) {
            return new View("/login.do", true);
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new View("/overtimeRequestMenu.do", true);
        }
        try {
            int requestId = Integer.parseInt(request.getParameter("requestId").trim());
            OvertimeRequest withdrawn = submitOvertimeRequestUseCase.withdraw(employeeId, requestId);
            session.setAttribute("overtimeRequestSuccessMessage", withdrawn.getTargetDate()
                    + " の残業事前申請を取り下げました。内容を直して申請し直せます。");
        } catch (NumberFormatException | NullPointerException e) {
            session.setAttribute("overtimeRequestErrorMessage", "取り下げる申請が分かりません。画面を読み込み直してください。");
        } catch (RuntimeException e) {
            session.setAttribute("overtimeRequestErrorMessage", e.getMessage());
        }
        return new View("/overtimeRequestMenu.do", true);
    }
}
