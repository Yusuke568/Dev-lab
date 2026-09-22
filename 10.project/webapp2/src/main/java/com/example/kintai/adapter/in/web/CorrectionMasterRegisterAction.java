package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.RegisterCorrectionMasterUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 補正マスタを新規登録するアクション。
 */
public class CorrectionMasterRegisterAction implements Action {

    private final RegisterCorrectionMasterUseCase registerCorrectionMasterUseCase;

    public CorrectionMasterRegisterAction(RegisterCorrectionMasterUseCase registerCorrectionMasterUseCase) {
        this.registerCorrectionMasterUseCase = registerCorrectionMasterUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String name = request.getParameter("name");
            int correctionUsTime = parseTimeToMinutes(request.getParameter("correctionUsTime"));
            int correctionMidTime = parseTimeToMinutes(request.getParameter("correctionMidTime"));

            registerCorrectionMasterUseCase.register(name, correctionUsTime, correctionMidTime);
        } catch (Exception e) {
            request.getSession().setAttribute("correctionMasterErrorMessage", e.getMessage());
        }

        return new View("/correctionMasterList.do", true);
    }

    private int parseTimeToMinutes(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) {
            return 0;
        }
        String[] parts = hhmm.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}
