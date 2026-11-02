package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.GetNextShainIdUseCase;
import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 社員登録フォームを表示するアクション。
 */
public class ShainInsertFormAction implements Action {

    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final GetNextShainIdUseCase getNextShainIdUseCase;
    private final com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase;

    public ShainInsertFormAction(GetAllClassmastersUseCase getAllClassmastersUseCase, GetNextShainIdUseCase getNextShainIdUseCase,
                                 com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase) {
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
        this.getNextShainIdUseCase = getNextShainIdUseCase;
        this.masterUseCase = masterUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("jobList", getAllClassmastersUseCase.getAllClassmasters());
        request.setAttribute("yearList", entryYears());
        request.setAttribute("departments", masterUseCase.getDepartmentTree());
        request.setAttribute("nextId", getNextShainIdUseCase.getNextId().getValue());
        com.example.shared.web.CsrfToken.getOrCreate(request.getSession());
        return new View("/WEB-INF/view/shaininsert.jsp");
    }

    /** 入社年の選択肢（今年から1960年まで、新しい順）。 */
    private static List<Integer> entryYears() {
        List<Integer> years = new ArrayList<>();
        for (int y = Year.now().getValue(); y >= 1960; y--) {
            years.add(y);
        }
        return years;
    }
}
