package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.UpdateShainUseCase;
import com.example.shain.domain.model.InvalidShainIdException;
import com.example.shain.domain.model.ShainId;
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
 * 社員更新を実行するアクション。社員IDの変更もここで行う。
 */
public class ShainUpdateExecuteAction implements Action {

    private final UpdateShainUseCase updateShainUseCase;
    private final com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase;
    /** 入力エラーのとき、選択肢を用意し直して更新画面を表示するために使う */
    private final ShainUpdateFormAction formAction;

    public ShainUpdateExecuteAction(UpdateShainUseCase updateShainUseCase,
                                    com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase,
                                    ShainUpdateFormAction formAction) {
        this.updateShainUseCase = updateShainUseCase;
        this.masterUseCase = masterUseCase;
        this.formAction = formAction;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new View("/shainList.do", true);
        }
        CsrfToken.preventCaching(response);
        if (!CsrfToken.isValid(request)) {
            return showErrors(request, response, List.of("画面の有効期限が切れました。もう一度入力してください。"));
        }

        String currentId = request.getParameter("id");
        UpdateShainUseCase.UpdateShainCommand command = new UpdateShainUseCase.UpdateShainCommand(
                currentId,
                request.getParameter("newId"),
                request.getParameter("name"),
                request.getParameter("namekana"),
                Integer.parseInt(request.getParameter("entryyear")),
                request.getParameter("gender"),
                request.getParameter("jobclass")
        );

        ShainId updatedId;
        try {
            updatedId = updateShainUseCase.updateShain(command);
        } catch (RuntimeException e) {
            InvalidShainIdException invalid = InvalidShainIdException.findIn(e).orElseThrow(() -> e);
            return showErrors(request, response, List.of(invalid.getMessage()));
        }

        String departmentId = request.getParameter("departmentId");
        masterUseCase.assignDepartment(updatedId.getIntValue(),
                departmentId == null || departmentId.isBlank() ? null : Integer.valueOf(departmentId));

        // 自分自身のIDを変えたときは、ログイン中の情報も新しいIDにする
        HttpSession session = request.getSession(false);
        if (session != null && currentId != null && currentId.equals(session.getAttribute("loginUser"))
                && !currentId.equals(updatedId.getValue())) {
            session.setAttribute("loginUser", updatedId.getValue());
        }
        return new View("/shainList.do", true);
    }

    /** エラーを表示して更新画面に戻す。入力値は JSP が param から戻す。 */
    private View showErrors(HttpServletRequest request, HttpServletResponse response, List<String> errors)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        return formAction.execute(request, response);
    }
}
