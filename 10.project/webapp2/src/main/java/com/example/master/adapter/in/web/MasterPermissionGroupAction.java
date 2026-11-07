package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;
import com.example.permission.domain.model.Feature;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 権限グループ（グループ名と使える機能）の一覧・追加・更新。
 */
public class MasterPermissionGroupAction extends MasterActionSupport {

    public MasterPermissionGroupAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterPermissionGroup.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("groups", masterUseCase.getPermissionGroups());
        request.setAttribute("features", Feature.values());
        // 列見出しで、マスタ管理の機能を「マスタ管理」の下にまとめるため
        request.setAttribute("masterFeatureCount", Feature.masterFeatures().size());
        return "/WEB-INF/view/master/master_permission_group.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        Integer id = optionalInt(request, "id");
        String[] codes = request.getParameterValues("feature");
        Set<String> featureCodes = codes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(Arrays.asList(codes));
        masterUseCase.savePermissionGroup(id, request.getParameter("name"), featureCodes);
        return (id == null ? "権限グループを追加しました。" : "権限グループを更新しました。")
                + "（ログイン中の社員には、次にログインしたときから反映されます）";
    }

    @Override
    protected String delete(HttpServletRequest request) {
        masterUseCase.deletePermissionGroup(requiredInt(request, "id"));
        return "権限グループを削除しました。";
    }
}
