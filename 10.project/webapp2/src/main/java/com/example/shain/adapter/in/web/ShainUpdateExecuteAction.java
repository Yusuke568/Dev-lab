package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.UpdateShainUseCase;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員更新を実行するアクション。
 */
public class ShainUpdateExecuteAction implements Action {

    private final UpdateShainUseCase updateShainUseCase;

    public ShainUpdateExecuteAction(UpdateShainUseCase updateShainUseCase) {
        this.updateShainUseCase = updateShainUseCase;
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
        return new View("/shainList.do", true);
    }
}
