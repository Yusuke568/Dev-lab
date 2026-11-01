package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;

/**
 * 組織図（部署の親子・承認者）と、社員の所属部署の設定。
 * フォームの mode が "assign" なら社員の所属を、それ以外なら部署を保存する。
 */
public class MasterDepartmentAction extends MasterActionSupport {

    public MasterDepartmentAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterDepartment.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("departments", masterUseCase.getDepartmentTree());
        request.setAttribute("staffList", masterUseCase.getStaffOptions());
        return "/WEB-INF/view/master/master_department.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        if ("assign".equals(request.getParameter("mode"))) {
            masterUseCase.assignDepartment(requiredInt(request, "staffId"), optionalInt(request, "departmentId"));
            return "所属部署を更新しました。";
        }
        Integer id = optionalInt(request, "id");
        Integer sortOrder = optionalInt(request, "sortOrder");
        masterUseCase.saveDepartment(id, request.getParameter("name"), optionalInt(request, "parentId"),
                optionalInt(request, "approverStaffId"), sortOrder != null ? sortOrder : 0);
        return id == null ? "部署を追加しました。" : "部署を更新しました。";
    }
}
