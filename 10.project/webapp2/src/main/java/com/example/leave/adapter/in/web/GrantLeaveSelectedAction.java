package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.AddLeaveDaysToEmployeesUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 選択した社員に有給を一括付与するアクション。
 */
public class GrantLeaveSelectedAction implements Action {

    private final AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase;

    public GrantLeaveSelectedAction(AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase) {
        this.addLeaveDaysToEmployeesUseCase = addLeaveDaysToEmployeesUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] ids = request.getParameterValues("shainIds");
        String daysStr = request.getParameter("grantDays");

        if (ids != null && daysStr != null) {
            List<ShainId> shainIds = Arrays.stream(ids)
                    .map(ShainId::new)
                    .collect(Collectors.toList());
            addLeaveDaysToEmployeesUseCase.addLeaveDaysToEmployees(shainIds, Integer.parseInt(daysStr));
        }

        return new View("/paidLeaveAdmin.do", true);
    }
}
