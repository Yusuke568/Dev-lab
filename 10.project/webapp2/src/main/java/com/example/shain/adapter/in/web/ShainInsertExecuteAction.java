package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.RegisterShainUseCase;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員登録を実行するアクション。
 */
public class ShainInsertExecuteAction implements Action {

    private final RegisterShainUseCase registerShainUseCase;

    public ShainInsertExecuteAction(RegisterShainUseCase registerShainUseCase) {
        this.registerShainUseCase = registerShainUseCase;
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

        registerShainUseCase.registerShain(command);
        return new View("/shainList.do", true);
    }
}
