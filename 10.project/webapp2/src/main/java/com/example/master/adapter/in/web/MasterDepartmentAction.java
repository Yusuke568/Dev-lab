package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 組織図（部署の親子・承認者・副承認者）と、社員の所属部署・兼任部署の設定。
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
        // JSP で「選択済みか」を調べやすいよう Set にする
        request.setAttribute("subApprovers", toSets(masterUseCase.getSubApprovers()));
        request.setAttribute("concurrentDepartments", toSets(masterUseCase.getAllConcurrentDepartments()));
        return "/WEB-INF/view/master/master_department.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        if ("assign".equals(request.getParameter("mode"))) {
            int staffId = requiredInt(request, "staffId");
            masterUseCase.assignDepartment(staffId, optionalInt(request, "departmentId"));
            masterUseCase.assignConcurrentDepartments(staffId, intValues(request, "concurrentDepartmentIds"));
            return "所属部署を更新しました。";
        }
        Integer id = optionalInt(request, "id");
        Integer sortOrder = optionalInt(request, "sortOrder");
        masterUseCase.saveDepartment(id, request.getParameter("name"), optionalInt(request, "parentId"),
                optionalInt(request, "approverStaffId"), intValues(request, "subApproverIds"),
                sortOrder != null ? sortOrder : 0);
        return id == null ? "部署を追加しました。" : "部署を更新しました。";
    }

    /** 複数選択の値（空欄は除く） */
    static List<Integer> intValues(HttpServletRequest request, String name) {
        List<Integer> values = new ArrayList<>();
        String[] raw = request.getParameterValues(name);
        if (raw != null) {
            for (String v : raw) {
                if (v != null && !v.isBlank()) {
                    values.add(Integer.valueOf(v.trim()));
                }
            }
        }
        return values;
    }

    private static Map<Integer, Set<Integer>> toSets(Map<Integer, List<Integer>> lists) {
        Map<Integer, Set<Integer>> sets = new HashMap<>();
        lists.forEach((k, v) -> sets.put(k, new HashSet<>(v)));
        return sets;
    }
}
