package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.GetCorrectionMastersUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 補正マスタの一覧・登録画面を表示するアクション。
 */
public class CorrectionMasterListAction implements Action {

    private final GetCorrectionMastersUseCase getCorrectionMastersUseCase;

    public CorrectionMasterListAction(GetCorrectionMastersUseCase getCorrectionMastersUseCase) {
        this.getCorrectionMastersUseCase = getCorrectionMastersUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("correctionMasters", getCorrectionMastersUseCase.getCorrectionMasters());

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("correctionMasterErrorMessage") != null) {
            request.setAttribute("errorMessage", session.getAttribute("correctionMasterErrorMessage"));
            session.removeAttribute("correctionMasterErrorMessage");
        }

        return new View("/WEB-INF/view/correction_master.jsp");
    }
}
