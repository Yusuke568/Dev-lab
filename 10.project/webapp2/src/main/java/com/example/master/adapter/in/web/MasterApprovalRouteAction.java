package com.example.master.adapter.in.web;

import com.example.approval.domain.model.RequestType;
import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;

/**
 * 承認ルート設定（申請種別ごとに、組織図を何段上の承認者までたどるか）。
 */
public class MasterApprovalRouteAction extends MasterActionSupport {

    public MasterApprovalRouteAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterApprovalRoute.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("settings", masterUseCase.getApprovalRouteSettings());
        return "/WEB-INF/view/master/master_approval_route.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        RequestType type = RequestType.valueOf(request.getParameter("requestType"));
        masterUseCase.saveApprovalRouteSetting(type, requiredInt(request, "maxLevels"));
        return type.getLabel() + "の承認段数を保存しました。（これから出される申請に反映されます）";
    }
}
