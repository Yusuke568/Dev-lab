package com.example.leave.adapter.in.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 有給休暇管理画面の操作結果を、リダイレクト後の画面に1回だけ出すためのメッセージ。
 */
public final class PaidLeaveFlash {

    private static final String MESSAGE = "paidLeaveMessage";
    private static final String ERROR = "paidLeaveErrorMessage";

    private PaidLeaveFlash() {
    }

    public static void message(HttpServletRequest request, String text) {
        request.getSession().setAttribute(MESSAGE, text);
    }

    public static void error(HttpServletRequest request, String text) {
        request.getSession().setAttribute(ERROR, text);
    }

    /** セッションに残したメッセージを request の message / errorMessage に移す */
    public static void moveToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        move(session, request, MESSAGE, "message");
        move(session, request, ERROR, "errorMessage");
    }

    private static void move(HttpSession session, HttpServletRequest request, String key, String attr) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(attr, value);
            session.removeAttribute(key);
        }
    }
}
