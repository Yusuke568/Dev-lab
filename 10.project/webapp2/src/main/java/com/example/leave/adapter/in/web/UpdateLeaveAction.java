package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.UpdatePaidLeaveDaysUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 有給日数を更新するアクション。
 */
public class UpdateLeaveAction implements Action {

    private final UpdatePaidLeaveDaysUseCase updatePaidLeaveDaysUseCase;

    public UpdateLeaveAction(UpdatePaidLeaveDaysUseCase updatePaidLeaveDaysUseCase) {
        this.updatePaidLeaveDaysUseCase = updatePaidLeaveDaysUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String daysStr = request.getParameter("days");

        if (idStr != null && daysStr != null) {
            try {
                updatePaidLeaveDaysUseCase.updatePaidLeaveDays(new ShainId(idStr), Double.parseDouble(daysStr.trim()));
                PaidLeaveFlash.message(request, "社員 " + idStr + " の有給休暇残日数を " + daysStr.trim() + " 日にしました。");
            } catch (NumberFormatException e) {
                PaidLeaveFlash.error(request, "有給休暇残日数は数字で入れてください。");
            } catch (RuntimeException e) {
                PaidLeaveFlash.error(request, e.getMessage());
            }
        }

        return new View("/paidLeaveAdmin.do", true);
    }
}
