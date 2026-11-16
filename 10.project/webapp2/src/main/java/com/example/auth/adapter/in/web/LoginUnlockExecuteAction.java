package com.example.auth.adapter.in.web;

import com.example.auth.domain.service.LoginAttemptLimiter;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ログインに続けて失敗して止まっているユーザー名・接続元を、管理者が解除する（社員一覧の画面から）。
 * POST だけ受け付け、CSRF のトークンは FrontController で確かめる。
 */
public class LoginUnlockExecuteAction implements Action {

    /** 結果を社員一覧の画面に1回だけ出すためのセッションのキー */
    public static final String FLASH_MESSAGE = "shainListMessage";
    public static final String FLASH_ERROR = "shainListErrorMessage";

    private static final Logger LOG = Logger.getLogger(LoginUnlockExecuteAction.class.getName());

    private final LoginAttemptLimiter loginAttemptLimiter;

    public LoginUnlockExecuteAction(LoginAttemptLimiter loginAttemptLimiter) {
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String kind = request.getParameter("kind");
        String value = request.getParameter("value");
        if (value == null || value.isBlank()) {
            session.setAttribute(FLASH_ERROR, "解除するロックが分かりません。画面を読み込み直してください。");
            return new View("/shainList.do", true);
        }

        boolean unlocked;
        String label;
        if ("ACCOUNT".equals(kind)) {
            unlocked = loginAttemptLimiter.unlockAccount(value);
            label = "ユーザー名「" + value + "」";
        } else if ("ADDRESS".equals(kind)) {
            unlocked = loginAttemptLimiter.unlockAddress(value);
            label = "接続元「" + value + "」";
        } else {
            session.setAttribute(FLASH_ERROR, "解除するロックが分かりません。画面を読み込み直してください。");
            return new View("/shainList.do", true);
        }

        if (unlocked) {
            LOG.info("ログインのロックを解除しました（" + forLog(label) + "、解除した人 " + session.getAttribute("loginUser") + "）");
            session.setAttribute(FLASH_MESSAGE, label + "のログインのロックを解除しました。");
        } else {
            session.setAttribute(FLASH_MESSAGE, label + "は、すでにロックが解除されています。");
        }
        return new View("/shainList.do", true);
    }

    private static String forLog(String value) {
        String v = value.replaceAll("\\p{Cntrl}", "?");
        return v.length() > 100 ? v.substring(0, 100) + "…" : v;
    }
}
