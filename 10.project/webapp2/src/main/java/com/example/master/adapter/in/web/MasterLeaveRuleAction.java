package com.example.master.adapter.in.web;

import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;

/**
 * 有給付与ルール（勤続年数ごとの付与日数）の一覧・追加・更新・削除。
 */
public class MasterLeaveRuleAction extends MasterActionSupport {

    public MasterLeaveRuleAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterLeaveRule.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("rules", masterUseCase.getLeaveGrantRules());
        return "/WEB-INF/view/master/master_leave_rule.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        int years = requiredInt(request, "yearsOfService");
        if ("delete".equals(request.getParameter("mode"))) {
            masterUseCase.deleteLeaveGrantRule(years);
            return "勤続" + years + "年のルールを削除しました。";
        }
        masterUseCase.saveLeaveGrantRule(years, requiredInt(request, "grantDays"));
        return "勤続" + years + "年のルールを保存しました。";
    }
}
