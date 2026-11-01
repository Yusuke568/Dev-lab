package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.UpdateShainUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員更新を実行するアクション。
 */
public class ShainUpdateExecuteAction implements Action {

    private final UpdateShainUseCase updateShainUseCase;
    private final com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase;

    public ShainUpdateExecuteAction(UpdateShainUseCase updateShainUseCase, com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase) {
        this.updateShainUseCase = updateShainUseCase;
        this.masterUseCase = masterUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UpdateShainUseCase.UpdateShainCommand command = new UpdateShainUseCase.UpdateShainCommand(
                request.getParameter("id"),
                request.getParameter("name"),
                request.getParameter("namekana"),
                Integer.parseInt(request.getParameter("entryyear")),
                request.getParameter("gender"),
                request.getParameter("jobclass")
        );

        updateShainUseCase.updateShain(command);
        String departmentId = request.getParameter("departmentId");
        masterUseCase.assignDepartment(Integer.parseInt(request.getParameter("id")),
                departmentId == null || departmentId.isBlank() ? null : Integer.valueOf(departmentId));
        return new View("/shainList.do", true);
    }
}
