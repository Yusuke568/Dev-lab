package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.GetShainByIdUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員更新フォームを表示するアクション。
 */
public class ShainUpdateFormAction implements Action {

    private final GetShainByIdUseCase getShainByIdUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase;

    public ShainUpdateFormAction(GetShainByIdUseCase getShainByIdUseCase, GetAllClassmastersUseCase getAllClassmastersUseCase,
                                 com.example.master.application.port.in.MasterMaintenanceUseCase masterUseCase) {
        this.getShainByIdUseCase = getShainByIdUseCase;
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
        this.masterUseCase = masterUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            return new View("/WEB-INF/view/error.jsp");
        }

        ShainId id = new ShainId(idStr);
        return getShainByIdUseCase.getShainById(id)
                .map(shain -> {
                    request.setAttribute("shain", new ShainDto(shain));
                    request.setAttribute("jobList", getAllClassmastersUseCase.getAllClassmasters());
                    request.setAttribute("departments", masterUseCase.getDepartmentTree());
                    request.setAttribute("currentDepartmentId", masterUseCase.getDepartmentIdOfStaff(id.getIntValue()));
                    request.setAttribute("concurrentDepartmentIds",
                            new java.util.HashSet<>(masterUseCase.getConcurrentDepartmentIds(id.getIntValue())));
                    com.example.shared.web.CsrfToken.getOrCreate(request.getSession());
                    return new View("/WEB-INF/view/shainupdate.jsp");
                })
                .orElse(new View("/WEB-INF/view/error.jsp"));
    }
}
