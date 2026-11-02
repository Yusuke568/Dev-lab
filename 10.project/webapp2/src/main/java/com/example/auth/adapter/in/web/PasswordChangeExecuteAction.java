package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.ChangeOwnPasswordUseCase;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 本人によるパスワード変更を実行するアクション。
 * 変更するユーザーはリクエストのパラメータからは受け取らず、セッションのログインユーザーに限る。
 */
public class PasswordChangeExecuteAction implements Action {

    private final ChangeOwnPasswordUseCase changeOwnPasswordUseCase;

    public PasswordChangeExecuteAction(ChangeOwnPasswordUseCase changeOwnPasswordUseCase) {
        this.changeOwnPasswordUseCase = changeOwnPasswordUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new View("/passwordChange.do", true);
        }
        CsrfToken.preventCaching(response);
        if (!CsrfToken.isValid(request)) {
            return showErrors(request, List.of("画面の有効期限が切れました。もう一度入力してください。"));
        }

        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("loginUser");
        try {
            changeOwnPasswordUseCase.changePassword(username,
                    request.getParameter("currentPassword"),
                    request.getParameter("newPassword"),
                    request.getParameter("confirmPassword"));
        } catch (PasswordValidationException e) {
            return showErrors(request, e.getErrors());
        }

        session.removeAttribute("mustChangePassword");
        // パスワードが変わったので、セッションIDも新しくする
        request.changeSessionId();
        session.setAttribute(PasswordChangeAction.DONE_FLASH, Boolean.TRUE);
        return new View("/passwordChange.do", true);
    }

    /** エラーを表示して入力画面に戻す。入力されたパスワードは画面に戻さない。 */
    private static View showErrors(HttpServletRequest request, List<String> errors) {
        CsrfToken.getOrCreate(request.getSession());
        request.setAttribute("errors", errors);
        return new View("/WEB-INF/view/password_change.jsp");
    }
}
