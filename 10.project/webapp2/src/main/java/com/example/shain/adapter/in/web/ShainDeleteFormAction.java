package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.GetShainByIdUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員削除確認フォームを表示するアクション。
 */
public class ShainDeleteFormAction implements Action {

    private final GetShainByIdUseCase getShainByIdUseCase;

    public ShainDeleteFormAction(GetShainByIdUseCase getShainByIdUseCase) {
        this.getShainByIdUseCase = getShainByIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            return new View("/WEB-INF/view/error.jsp");
        }

        ShainId id = new ShainId(idStr);
        return getShainByIdUseCase.getShainById(id)
                .map(shain -> {
                    request.setAttribute("shain", new ShainDto(shain));
                    return new View("/WEB-INF/view/shaindelete.jsp");
                })
                .orElse(new View("/WEB-INF/view/error.jsp"));
    }
}
