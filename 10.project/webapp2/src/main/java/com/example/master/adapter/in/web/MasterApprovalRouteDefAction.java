package com.example.master.adapter.in.web;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.ResolvedRoute;
import com.example.approval.domain.model.RouteAssignment;
import com.example.master.application.port.in.MasterMaintenanceUseCase;
import com.example.master.domain.model.Department;
import com.example.master.domain.model.StaffOption;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 承認ルートマスタ。段ごとに承認者（複数可）を並べたルートを作り、社員や部署に割り当てる。
 * 割り当てのない社員・部署は、これまでどおり組織図で承認ルートが決まる。
 * 画面の下で「この社員の申請はどのルートになるか」を確かめられる。
 */
public class MasterApprovalRouteDefAction extends MasterActionSupport {

    private final ApprovalWorkflowService approvalWorkflowService;

    public MasterApprovalRouteDefAction(MasterMaintenanceUseCase masterUseCase, ApprovalWorkflowService approvalWorkflowService) {
        super(masterUseCase);
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    protected String selfPath() {
        return "/masterApprovalRouteDef.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        List<StaffOption> staff = masterUseCase.getStaffOptions();
        List<Department> departments = masterUseCase.getDepartmentTree();
        Map<Integer, String> staffNames = new HashMap<>();
        staff.forEach(s -> staffNames.put(s.getId(), s.getName()));

        List<ApprovalRoute> routes = masterUseCase.getApprovalRoutes();
        List<RouteView> views = routes.stream().map(r -> new RouteView(r, staffNames)).collect(Collectors.toList());
        request.setAttribute("routes", views);
        request.setAttribute("staffList", staff);
        request.setAttribute("departments", departments);
        request.setAttribute("requestTypes", RequestType.values());

        // 編集するルート（?edit=ID）。なければ新規
        Integer editId = optionalInt(request, "edit");
        ApprovalRoute editing = editId == null ? null
                : routes.stream().filter(r -> editId.equals(r.getId())).findFirst().orElse(null);
        request.setAttribute("editing", editing != null ? new RouteView(editing, staffNames) : null);

        // ルートの確認（?previewStaffId=…&previewType=…&previewDept=…）
        Integer previewStaff = optionalInt(request, "previewStaffId");
        if (previewStaff != null) {
            String typeParam = request.getParameter("previewType");
            RequestType type = typeParam == null || typeParam.isBlank() ? RequestType.OVERTIME : RequestType.valueOf(typeParam);
            Integer previewDept = optionalInt(request, "previewDept");
            request.setAttribute("previewStaffId", previewStaff);
            request.setAttribute("previewType", type.name());
            request.setAttribute("previewDept", previewDept);
            try {
                ResolvedRoute route = approvalWorkflowService.previewRoute(type, previewStaff, previewDept);
                request.setAttribute("previewSource", route.getSource());
                request.setAttribute("previewLevels", levelLabels(route.getLevels(), staffNames));
            } catch (RuntimeException e) {
                request.setAttribute("previewError", e.getMessage());
            }
        }
        return "/WEB-INF/view/master/master_approval_route_def.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        if ("delete".equals(request.getParameter("mode"))) {
            masterUseCase.deleteApprovalRoute(requiredInt(request, "id"));
            return "承認ルートを削除しました。";
        }
        Integer id = optionalInt(request, "id");
        String typeParam = request.getParameter("requestType");
        RequestType type = typeParam == null || typeParam.isBlank() ? null : RequestType.valueOf(typeParam);

        // 段は level1, level2, … の複数選択（levelCount 段まで）
        int levelCount = requiredInt(request, "levelCount");
        List<List<Integer>> levels = new ArrayList<>();
        for (int i = 1; i <= Math.min(levelCount, 20); i++) {
            levels.add(MasterDepartmentAction.intValues(request, "level" + i));
        }
        List<RouteAssignment> assignments = new ArrayList<>();
        for (Integer staffId : MasterDepartmentAction.intValues(request, "assignStaffIds")) {
            assignments.add(new RouteAssignment(RouteAssignment.STAFF, staffId, null));
        }
        for (Integer deptId : MasterDepartmentAction.intValues(request, "assignDepartmentIds")) {
            assignments.add(new RouteAssignment(RouteAssignment.DEPARTMENT, deptId, null));
        }
        masterUseCase.saveApprovalRoute(id, request.getParameter("name"), type, request.getParameter("note"),
                levels, assignments);
        return id == null ? "承認ルートを追加しました。" : "承認ルートを更新しました。";
    }

    /** 段ごとの承認者を「1. 山田／佐藤」の形にする */
    static List<String> levelLabels(List<List<Integer>> levels, Map<Integer, String> staffNames) {
        List<String> labels = new ArrayList<>();
        int n = 1;
        for (List<Integer> level : levels) {
            String names = level.stream()
                    .map(id -> staffNames.getOrDefault(id, String.valueOf(id)))
                    .collect(Collectors.joining("／"));
            labels.add(n++ + ". " + names);
        }
        return labels;
    }

    /** 画面表示用のルート */
    public static class RouteView {
        private final ApprovalRoute route;
        private final List<String> levelLabels;

        RouteView(ApprovalRoute route, Map<Integer, String> staffNames) {
            this.route = route;
            this.levelLabels = levelLabels(route.getLevels(), staffNames);
        }

        public Integer getId() { return route.getId(); }
        public String getName() { return route.getName(); }
        public String getNote() { return route.getNote(); }
        public String getRequestTypeCode() { return route.getRequestType() != null ? route.getRequestType().name() : ""; }
        public String getRequestTypeLabel() { return route.getRequestTypeLabel(); }
        public List<String> getLevelLabels() { return levelLabels; }
        public List<List<Integer>> getLevels() { return route.getLevels(); }

        /** 段ごとの承認者（JSP で選択済みかを調べる用） */
        public List<Set<Integer>> getLevelSets() {
            return route.getLevels().stream().map(HashSet::new).collect(Collectors.toList());
        }

        public List<RouteAssignment> getAssignments() { return route.getAssignments(); }

        public Set<Integer> getAssignedStaffIds() {
            return route.getAssignments().stream().filter(RouteAssignment::isStaff)
                    .map(RouteAssignment::getTargetId).collect(Collectors.toSet());
        }

        public Set<Integer> getAssignedDepartmentIds() {
            return route.getAssignments().stream().filter(a -> !a.isStaff())
                    .map(RouteAssignment::getTargetId).collect(Collectors.toSet());
        }
    }
}
