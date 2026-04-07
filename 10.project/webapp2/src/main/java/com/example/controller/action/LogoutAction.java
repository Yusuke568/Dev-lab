package com.example.controller.action;

import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ログアウト実行のアクションクラス。
 */
public class LogoutAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // セッションを破棄
        }
        return new View("/login.do", true); // リダイレクトでログイン画面へ
    }
}
