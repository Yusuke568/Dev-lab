package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.RegisterShainUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員登録を実行するアクション。
 */
public class ShainInsertExecuteAction implements Action {

    private final RegisterShainUseCase registerShainUseCase;
    private final com.example.shain.application.port.in.GetNextShainIdUseCase getNextShainIdUseCase;
    private final com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase;

    public ShainInsertExecuteAction(RegisterShainUseCase registerShainUseCase,
                                    com.example.shain.application.port.in.GetNextShainIdUseCase getNextShainIdUseCase,
                                    com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase) {
        this.registerShainUseCase = registerShainUseCase;
        this.getNextShainIdUseCase = getNextShainIdUseCase;
        this.masterUseCase = masterUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RegisterShainUseCase.RegisterShainCommand command = new RegisterShainUseCase.RegisterShainCommand(
                request.getParameter("name"),
                request.getParameter("namekana"),
                Integer.parseInt(request.getParameter("entryyear")),
                request.getParameter("gender"),
                request.getParameter("jobclass")
        );

        // 登録時の採番と同じ方法で、これから登録される社員IDを先に求めておく
        int newId = Integer.parseInt(getNextShainIdUseCase.getNextId().getValue());
        registerShainUseCase.registerShain(command);
        masterUseCase.assignDepartment(newId, parseDepartment(request));
        return new View("/shainList.do", true);
    }

    private static Integer parseDepartment(HttpServletRequest request) {
        String value = request.getParameter("departmentId");
        return value == null || value.isBlank() ? null : Integer.valueOf(value);
    }
}
