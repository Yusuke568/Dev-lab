package com.example.auth.adapter.in.web;

import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * パスワード変更画面を表示するアクション。ログインしている全員が使える。
 */
public class PasswordChangeAction implements Action {

    /** 変更が終わったことを、リダイレクト後の画面に一度だけ伝えるためのセッション属性 */
    static final String DONE_FLASH = "passwordChangeDone";

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        CsrfToken.getOrCreate(session);
        if (session.getAttribute(DONE_FLASH) != null) {
            session.removeAttribute(DONE_FLASH);
            request.setAttribute("successMessage", "パスワードを変更しました。");
        }
        CsrfToken.preventCaching(response);
        return new View("/WEB-INF/view/password_change.jsp");
    }
}
