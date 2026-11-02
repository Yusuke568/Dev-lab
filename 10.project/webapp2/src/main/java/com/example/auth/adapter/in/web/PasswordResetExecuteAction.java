package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.ResetPasswordUseCase;
import com.example.auth.domain.model.InitialPassword;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.shain.application.port.in.GetShainByIdUseCase;
import com.example.shain.domain.model.Shain;
import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 管理者によるパスワード初期化を実行するアクション（SHAIN_ADMIN のみ）。
 * 仮パスワードはリクエスト属性に入れて完了画面に一度だけ表示し、DB やログには平文で残さない。
 */
public class PasswordResetExecuteAction implements Action {

    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetShainByIdUseCase getShainByIdUseCase;

    public PasswordResetExecuteAction(ResetPasswordUseCase resetPasswordUseCase, GetShainByIdUseCase getShainByIdUseCase) {
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.getShainByIdUseCase = getShainByIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<Shain> shain = PasswordResetFormAction.findShain(getShainByIdUseCase, request.getParameter("id"));
        if (!"POST".equalsIgnoreCase(request.getMethod()) || shain.isEmpty()) {
            return new View("/shainList.do", true);
        }
        CsrfToken.preventCaching(response);
        request.setAttribute("shain", shain.get());
        if (!CsrfToken.isValid(request)) {
            return showErrors(request, List.of("画面の有効期限が切れました。もう一度操作してください。"));
        }

        InitialPassword initial = "specify".equals(request.getParameter("mode"))
                ? InitialPassword.specified(request.getParameter("newPassword"), request.getParameter("confirmPassword"))
                : InitialPassword.generated();
        @SuppressWarnings("unchecked")
        Set<String> features = (Set<String>) request.getSession().getAttribute("features");
        try {
            String temporaryPassword = resetPasswordUseCase.resetPassword(features, shain.get().getId().getValue(), initial);
            request.setAttribute("temporaryPassword", temporaryPassword);
        } catch (PasswordValidationException e) {
            return showErrors(request, e.getErrors());
        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", e.getMessage());
            return new View("/WEB-INF/view/error.jsp");
        }
        return new View("/WEB-INF/view/password_reset_done.jsp");
    }

    private static View showErrors(HttpServletRequest request, List<String> errors) {
        CsrfToken.getOrCreate(request.getSession());
        request.setAttribute("errors", errors);
        return new View("/WEB-INF/view/password_reset.jsp");
    }
}
