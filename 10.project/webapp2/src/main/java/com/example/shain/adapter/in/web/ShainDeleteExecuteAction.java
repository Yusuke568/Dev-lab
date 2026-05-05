package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.DeleteShainUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員削除を実行するアクション。
 */
public class ShainDeleteExecuteAction implements Action {

    private final DeleteShainUseCase deleteShainUseCase;

    public ShainDeleteExecuteAction(DeleteShainUseCase deleteShainUseCase) {
        this.deleteShainUseCase = deleteShainUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            deleteShainUseCase.deleteShain(new ShainId(idStr));
        }
        return new View("/shainList.do", true);
    }
}
