package controller.action;

import controller.Action;
import controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ログイン画面表示アクション
 */
public class LoginAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return new View("/WEB-INF/view/login.jsp");
    }
}
