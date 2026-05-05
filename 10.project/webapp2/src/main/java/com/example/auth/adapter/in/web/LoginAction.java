package com.example.auth.adapter.in.web;

import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ログイン画面を表示するアクション。
 */
public class LoginAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return new View("/WEB-INF/view/login.jsp");
    }
}
