package com.example.controller.action;

import com.example.application.port.in.AuthenticateUseCase;
import com.example.application.port.in.GetShainByIdUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ログイン実行のアクションクラス。
 */
public class LoginExecuteAction implements Action {

    private final AuthenticateUseCase authenticateUseCase;
    private final GetShainByIdUseCase getShainByIdUseCase;

    public LoginExecuteAction(AuthenticateUseCase authenticateUseCase, GetShainByIdUseCase getShainByIdUseCase) {
        this.authenticateUseCase = authenticateUseCase;
        this.getShainByIdUseCase = getShainByIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "ユーザー名とパスワードを入力してください。");
            return new View("/WEB-INF/view/login.jsp");
        }

        boolean isAuthenticated = authenticateUseCase.authenticate(username, password);

        if (isAuthenticated) {
            // ログイン成功
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", username);
            
            try {
                java.util.Optional<Shain> optionalShain = getShainByIdUseCase.getShainById(Integer.parseInt(username));
                if (optionalShain.isPresent()) {
                    session.setAttribute("loginUserRole", optionalShain.get().getJobclass()); // '0' for 一般, others for Admin/Manager
                }
            } catch (NumberFormatException e) {
                // Ignore parsing error
            }

            return new View("/menu.do", true); // Redirect to menu
        } else {
            // ログイン失敗
            request.setAttribute("errorMessage", "ユーザー名またはパスワードが間違っています。");
            return new View("/WEB-INF/view/login.jsp");
        }
    }
}
