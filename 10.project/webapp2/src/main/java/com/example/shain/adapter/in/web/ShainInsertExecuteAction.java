package com.example.shain.adapter.in.web;

import com.example.auth.domain.model.InitialPassword;
import com.example.auth.domain.model.PasswordValidationException;
import com.example.shain.application.port.in.RegisterShainUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 社員登録を実行するアクション。
 * 社員の登録と同時にログイン用のアカウントを作り、自動で作った仮パスワードは完了画面に一度だけ表示する。
 */
public class ShainInsertExecuteAction implements Action {

    private final RegisterShainUseCase registerShainUseCase;
    /** 入力エラーのとき、選択肢を用意し直して登録画面を表示するために使う */
    private final ShainInsertFormAction formAction;

    public ShainInsertExecuteAction(RegisterShainUseCase registerShainUseCase, ShainInsertFormAction formAction) {
        this.registerShainUseCase = registerShainUseCase;
        this.formAction = formAction;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new View("/shainInsertForm.do", true);
        }
        CsrfToken.preventCaching(response);
        if (!CsrfToken.isValid(request)) {
            return showErrors(request, response, List.of("画面の有効期限が切れました。もう一度入力してください。"));
        }

        InitialPassword initial = request.getParameter("generatePassword") != null
                ? InitialPassword.generated()
                : InitialPassword.specified(request.getParameter("initialPassword"), request.getParameter("initialPasswordConfirm"));
        int entryYear;
        try {
            entryYear = ShainFormValues.entryYear(request.getParameter("entryyear"));
        } catch (IllegalArgumentException e) {
            return showErrors(request, response, List.of(e.getMessage()));
        }
        RegisterShainUseCase.RegisterShainCommand command = new RegisterShainUseCase.RegisterShainCommand(
                request.getParameter("name"),
                request.getParameter("namekana"),
                entryYear,
                request.getParameter("gender"),
                request.getParameter("jobclass"),
                parseDepartment(request),
                initial
        );

        RegisterShainUseCase.RegisterShainResult result;
        try {
            result = registerShainUseCase.registerShain(command);
        } catch (RuntimeException e) {
            // TransactionManager に包まれて届くことがあるので、原因をたどって入力エラーかどうかを見る
            PasswordValidationException validation = PasswordValidationException.findIn(e).orElseThrow(() -> e);
            return showErrors(request, response, validation.getErrors());
        }

        request.setAttribute("shainId", result.getId().getValue());
        request.setAttribute("shainName", command.getName());
        request.setAttribute("temporaryPassword", result.getTemporaryPassword());
        return new View("/WEB-INF/view/shain_insert_done.jsp");
    }

    /** エラーを表示して登録画面に戻す。パスワード以外の入力値は JSP が param から戻す。 */
    private View showErrors(HttpServletRequest request, HttpServletResponse response, List<String> errors)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        return formAction.execute(request, response);
    }

    private static Integer parseDepartment(HttpServletRequest request) {
        String value = request.getParameter("departmentId");
        return value == null || value.isBlank() ? null : Integer.valueOf(value);
    }
}
