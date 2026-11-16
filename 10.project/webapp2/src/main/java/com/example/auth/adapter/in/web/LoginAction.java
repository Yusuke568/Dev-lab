package com.example.auth.adapter.in.web;

import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

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
        // ログインの送信にも CSRF のトークンを付ける（ほかのサイトから、攻撃者のアカウントでログインさせられないように）
        CsrfToken.getOrCreate(request.getSession());
        // ログアウトの後などに、古いトークンの入った画面をブラウザに使い回させない
        CsrfToken.preventCaching(response);
        return new View("/WEB-INF/view/login.jsp");
    }
}
