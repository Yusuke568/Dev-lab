package com.example.notice.adapter.in.web;

import com.example.notice.application.port.in.NoticeUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

/**
 * 上長がお知らせを作る・消す画面（APPROVAL 権限）。
 * 宛先は、組織図で自分が承認者になっている部署とその下の部署。
 */
public class NoticeManageAction implements Action {

    private static final String MESSAGE = "noticeMessage";
    private static final String ERROR = "noticeErrorMessage";

    private final NoticeUseCase noticeUseCase;

    public NoticeManageAction(NoticeUseCase noticeUseCase) {
        this.noticeUseCase = noticeUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String loginUser = (String) session.getAttribute("loginUser");

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (!CsrfToken.isValid(request)) {
                session.setAttribute(ERROR, "画面の有効期限が切れました。もう一度操作してください。");
            } else {
                try {
                    if ("delete".equals(request.getParameter("mode"))) {
                        noticeUseCase.delete(loginUser, Integer.parseInt(request.getParameter("id")));
                        session.setAttribute(MESSAGE, "お知らせを取り下げました。");
                    } else {
                        noticeUseCase.post(loginUser, Integer.parseInt(request.getParameter("departmentId")),
                                request.getParameter("body"), parseDate(request.getParameter("expiresOn")));
                        session.setAttribute(MESSAGE, "お知らせを出しました。宛先の部署のメンバーの勤怠画面に表示されます。");
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute(ERROR, "宛先の部署を選んでください。");
                } catch (IllegalArgumentException e) {
                    session.setAttribute(ERROR, e.getMessage());
                }
            }
            return new View("/noticeManage.do", true);
        }

        moveFlash(session, request, MESSAGE, "message");
        moveFlash(session, request, ERROR, "errorMessage");
        CsrfToken.getOrCreate(session);
        request.setAttribute("departments", noticeUseCase.getTargetDepartments(loginUser));
        request.setAttribute("notices", noticeUseCase.getMyNotices(loginUser));
        request.setAttribute("today", LocalDate.now());
        return new View("/WEB-INF/view/notice_manage.jsp");
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("掲載期限の日付の形式が正しくありません。");
        }
    }

    private static void moveFlash(HttpSession session, HttpServletRequest request, String key, String attr) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(attr, value);
            session.removeAttribute(key);
        }
    }
}
