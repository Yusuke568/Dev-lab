package com.example.controller.action;

import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ログイン画面表示のアクションクラス。
 */
public class LoginAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return new View("/WEB-INF/view/login.jsp");
    }
}
