package com.example.shared.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF 対策のトークン。
 * セッションに1つ保存し、フォームの hidden 項目（name="_csrf"）で送られてきた値と照合する。
 * JSP では ${sessionScope.csrfToken} で値を埋め込む。
 */
public final class CsrfToken {

    public static final String SESSION_KEY = "csrfToken";
    public static final String PARAMETER = "_csrf";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfToken() {
    }

    /**
     * セッションのトークンを返す。まだなければ作って保存する。
     */
    public static String getOrCreate(HttpSession session) {
        Object token = session.getAttribute(SESSION_KEY);
        if (token instanceof String) {
            return (String) token;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String created = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_KEY, created);
        return created;
    }

    /**
     * POST で送られてきたトークンがセッションのものと一致するか確かめる。
     */
    public static boolean isValid(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_KEY);
        String actual = request.getParameter(PARAMETER);
        if (!(expected instanceof String) || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(((String) expected).getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * パスワードなどを表示する画面を、ブラウザや途中のサーバーに保存させない。
     */
    public static void preventCaching(javax.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
