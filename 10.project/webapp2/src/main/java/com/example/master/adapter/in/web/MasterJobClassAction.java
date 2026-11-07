package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;

/**
 * 役職マスタ（役職名と権限グループの割り当て）の一覧・追加・更新。
 */
public class MasterJobClassAction extends MasterActionSupport {

    public MasterJobClassAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterJobClass.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("jobClasses", masterUseCase.getJobClasses());
        request.setAttribute("groups", masterUseCase.getPermissionGroups());
        return "/WEB-INF/view/master/master_job_class.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        Integer id = optionalInt(request, "id");
        masterUseCase.saveJobClass(id, request.getParameter("name"), optionalInt(request, "permissionGroupId"));
        return id == null ? "役職を追加しました。" : "役職を更新しました。";
    }

    @Override
    protected String delete(HttpServletRequest request) {
        masterUseCase.deleteJobClass(requiredInt(request, "id"));
        return "役職を削除しました。";
    }
}
