package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.AuthenticateUseCase;
import com.example.shain.application.port.in.GetShainByIdUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ログイン実行のアクション。
 */
public class LoginExecuteAction implements Action {

    private final AuthenticateUseCase authenticateUseCase;
    private final GetShainByIdUseCase getShainByIdUseCase;

    public LoginExecuteAction(AuthenticateUseCase authenticateUseCase, GetShainByIdUseCase getShainByIdUseCase) {
        this.authenticateUseCase = authenticateUseCase;
        this.getShainByIdUseCase = getShainByIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "ユーザー名とパスワードを入力してください。");
            return new View("/WEB-INF/view/login.jsp");
        }

        if (authenticateUseCase.authenticate(username, password)) {
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", username);
            
            getShainByIdUseCase.getShainById(new ShainId(username)).ifPresent(shain -> {
                session.setAttribute("loginUserRole", shain.getJobClass());
            });

            return new View("/menu.do", true);
        } else {
            request.setAttribute("errorMessage", "ユーザー名またはパスワードが間違っています。");
            return new View("/WEB-INF/view/login.jsp");
        }
    }
}
