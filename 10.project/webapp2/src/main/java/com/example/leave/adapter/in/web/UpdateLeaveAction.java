package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.UpdatePaidLeaveDaysUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

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
            updatePaidLeaveDaysUseCase.updatePaidLeaveDays(new ShainId(idStr), Integer.parseInt(daysStr));
        }

        return new View("/paidLeaveAdmin.do", true);
    }
}
