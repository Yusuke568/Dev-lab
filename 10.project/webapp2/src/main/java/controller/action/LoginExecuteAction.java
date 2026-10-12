package controller.action;

import controller.Action;
import controller.View;
import domain.auth.AuthRepository;
import domain.auth.UserSession;
import infrastructure.persistence.JdbcAuthRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * ログイン実行アクション
 */
public class LoginExecuteAction implements Action {

    private final AuthRepository authRepository = new JdbcAuthRepository();

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username != null && password != null) {
            Optional<UserSession> sessionOpt = authRepository.authenticate(username.trim(), password.trim());
            if (sessionOpt.isPresent()) {
                HttpSession session = request.getSession(true);
                session.setAttribute("userSession", sessionOpt.get());
                // ログイン成功時はSES勤怠画面へリダイレクト
                return new View("/sesAttendance.do", true);
            }
        }

        // ログイン失敗
        request.setAttribute("errorMessage", "社員IDまたはパスワードが正しくありません。");
        return new View("/WEB-INF/view/login.jsp");
    }
}
