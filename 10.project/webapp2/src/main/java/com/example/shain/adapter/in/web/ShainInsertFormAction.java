package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.GetNextShainIdUseCase;
import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員登録フォームを表示するアクション。
 */
public class ShainInsertFormAction implements Action {

    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final GetNextShainIdUseCase getNextShainIdUseCase;

    public ShainInsertFormAction(GetAllClassmastersUseCase getAllClassmastersUseCase, GetNextShainIdUseCase getNextShainIdUseCase) {
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
        this.getNextShainIdUseCase = getNextShainIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("classList", getAllClassmastersUseCase.getAllClassmasters());
        request.setAttribute("nextId", getNextShainIdUseCase.getNextId().getValue());
        return new View("/WEB-INF/view/shaininsert.jsp");
    }
}
