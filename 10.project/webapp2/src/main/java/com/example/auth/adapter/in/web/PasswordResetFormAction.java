package com.example.auth.adapter.in.web;

import com.example.shain.application.port.in.GetShainByIdUseCase;
import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 管理者がパスワードを初期化する画面を表示するアクション（SHAIN_ADMIN のみ）。
 */
public class PasswordResetFormAction implements Action {

    private final GetShainByIdUseCase getShainByIdUseCase;

    public PasswordResetFormAction(GetShainByIdUseCase getShainByIdUseCase) {
        this.getShainByIdUseCase = getShainByIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<Shain> shain = findShain(getShainByIdUseCase, request.getParameter("id"));
        if (shain.isEmpty()) {
            return new View("/shainList.do", true);
        }
        CsrfToken.getOrCreate(request.getSession());
        request.setAttribute("shain", shain.get());
        return new View("/WEB-INF/view/password_reset.jsp");
    }

    static Optional<Shain> findShain(GetShainByIdUseCase useCase, String id) {
        if (id == null || !id.matches("\\d{1,9}")) {
            return Optional.empty();
        }
        return useCase.getShainById(new ShainId(id));
    }
}
