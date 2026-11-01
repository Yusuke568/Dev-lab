package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;

/**
 * 適用マスタ（出勤・有給・休日などの勤務区分）の一覧・追加・更新。
 */
public class MasterAbstractAction extends MasterActionSupport {

    public MasterAbstractAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterAbstract.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("abstracts", masterUseCase.getAbstracts());
        return "/WEB-INF/view/master/master_abstract.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        boolean isNew = "new".equals(request.getParameter("mode"));
        masterUseCase.saveAbstract(isNew, requiredInt(request, "id"), request.getParameter("name"),
                checked(request, "isWork"), checked(request, "isPaid"),
                checked(request, "isLegalHoliday"), checked(request, "isPrescribedHoliday"));
        return isNew ? "区分を追加しました。" : "区分を更新しました。";
    }
}
