package com.example.master.adapter.in.web;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.RoutePreview;
import com.example.approval.domain.model.RouteAssignment;
import com.example.master.application.port.in.MasterMaintenanceUseCase;
import com.example.master.domain.model.ApprovalRouteSetting;
import com.example.master.domain.model.Department;
import com.example.master.domain.model.StaffOption;
import com.example.permission.domain.model.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 承認設定（組織図・承認段数・例外ルート・社員ごとの承認ルートを1画面で扱う）。
 *
 * 画面は JSP に埋め込んだ JSON を JavaScript（resources/js/master/approval-settings.js）が描く。
 * 保存はフォームの mode で振り分ける。
 *   staff  … 社員の主所属・兼任部署・例外ルート（組織図の権限）
 *   dept   … 部署（名前・上位部署・承認者・副承認者・並び順）（組織図の権限）
 *   levels … 組織図でたどる段数（承認ルート設定の権限）
 *   route / routeDelete … 例外ルートの保存・削除（承認ルート設定の権限）
 */
public class MasterApprovalSettingsAction extends MasterActionSupport {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ApprovalWorkflowService approvalWorkflowService;

    public MasterApprovalSettingsAction(MasterMaintenanceUseCase masterUseCase, ApprovalWorkflowService approvalWorkflowService) {
        super(masterUseCase);
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    protected String selfPath() {
        return "/masterApprovalSettings.do";
    }

    /** 保存後も、操作していたタブに戻る */
    @Override
    protected String redirectPath(HttpServletRequest request) {
        String tab = request.getParameter("tab");
        return tab != null && tab.matches("[a-z]{1,20}") ? selfPath() + "?tab=" + tab : selfPath();
    }

    @Override
    protected String show(HttpServletRequest request) {
        request.setAttribute("settingsJson", toScriptJson(buildData(request)));
        return "/WEB-INF/view/master/master_approval_settings.jsp";
    }

    /** 画面に渡すデータ（社員・部署・段数・例外ルート・社員ごとのルート） */
    Map<String, Object> buildData(HttpServletRequest request) {
        List<StaffOption> staff = masterUseCase.getStaffOptions();
        List<Department> departments = masterUseCase.getDepartmentTree();
        Map<Integer, List<Integer>> concurrent = masterUseCase.getAllConcurrentDepartments();
        Map<Integer, List<Integer>> subs = masterUseCase.getSubApprovers();
        List<ApprovalRoute> routes = masterUseCase.getApprovalRoutes();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contextPath", request.getContextPath());
        data.put("canDepartment", has(request, Feature.MASTER_DEPARTMENT));
        data.put("canRoute", has(request, Feature.MASTER_APPROVAL_ROUTE));
        data.put("tab", request.getParameter("tab"));

        List<Map<String, Object>> staffList = new ArrayList<>();
        Map<Integer, Integer> primary = new LinkedHashMap<>();
        for (StaffOption s : staff) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("deptId", s.getDepartmentId());
            m.put("concurrent", concurrent.getOrDefault(s.getId(), List.of()));
            staffList.add(m);
            primary.put(s.getId(), s.getDepartmentId());
        }
        data.put("staff", staffList);

        List<Map<String, Object>> deptList = new ArrayList<>();
        for (Department d : departments) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("name", d.getName());
            m.put("parentId", d.getParentId());
            m.put("depth", d.getDepth());
            m.put("approverId", d.getApproverStaffId());
            m.put("subIds", subs.getOrDefault(d.getId(), List.of()));
            m.put("sortOrder", d.getSortOrder());
            m.put("memberCount", d.getMemberCount());
            deptList.add(m);
        }
        data.put("departments", deptList);

        Map<String, Object> levels = new LinkedHashMap<>();
        Map<String, Object> typeLabels = new LinkedHashMap<>();
        for (ApprovalRouteSetting s : masterUseCase.getApprovalRouteSettings()) {
            levels.put(s.getRequestType().name(), s.getMaxLevels());
            typeLabels.put(s.getRequestType().name(), s.getRequestType().getLabel());
        }
        data.put("maxLevels", levels);
        data.put("requestTypes", typeLabels);

        List<Map<String, Object>> routeList = new ArrayList<>();
        for (ApprovalRoute r : routes) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("type", r.getRequestType() != null ? r.getRequestType().name() : null);
            m.put("note", r.getNote());
            m.put("levels", r.getLevels());
            m.put("staffIds", r.getAssignments().stream().filter(RouteAssignment::isStaff).map(RouteAssignment::getTargetId).toList());
            m.put("deptIds", r.getAssignments().stream().filter(a -> !a.isStaff()).map(RouteAssignment::getTargetId).toList());
            routeList.add(m);
        }
        data.put("routes", routeList);

        Map<String, Object> previews = new LinkedHashMap<>();
        for (RequestType type : RequestType.values()) {
            Map<String, Object> byStaff = new LinkedHashMap<>();
            approvalWorkflowService.previewAll(type, primary).forEach((id, p) -> byStaff.put(String.valueOf(id), toMap(p)));
            previews.put(type.name(), byStaff);
        }
        data.put("previews", previews);
        return data;
    }

    private static Map<String, Object> toMap(RoutePreview p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("levels", p.levels());
        m.put("source", p.source());
        m.put("auto", p.autoApproved());
        m.put("error", p.error());
        return m;
    }

    @Override
    protected String save(HttpServletRequest request) {
        String mode = request.getParameter("mode");
        switch (mode == null ? "" : mode) {
            case "staff": {
                require(request, Feature.MASTER_DEPARTMENT);
                int staffId = requiredInt(request, "staffId");
                masterUseCase.assignDepartment(staffId, optionalInt(request, "departmentId"));
                masterUseCase.assignConcurrentDepartments(staffId, MasterDepartmentAction.intValues(request, "concurrentDepartmentIds"));
                if (has(request, Feature.MASTER_APPROVAL_ROUTE)) {
                    masterUseCase.assignStaffRoutes(staffId, optionalInt(request, "overtimeRouteId"), optionalInt(request, "leaveRouteId"));
                }
                return "社員 " + staffId + " の所属と承認ルートを更新しました。";
            }
            case "dept": {
                require(request, Feature.MASTER_DEPARTMENT);
                Integer id = optionalInt(request, "id");
                Integer sortOrder = optionalInt(request, "sortOrder");
                masterUseCase.saveDepartment(id, request.getParameter("name"), optionalInt(request, "parentId"),
                        optionalInt(request, "approverStaffId"), MasterDepartmentAction.intValues(request, "subApproverIds"),
                        sortOrder != null ? sortOrder : 0);
                return id == null ? "部署を追加しました。" : "部署を更新しました。";
            }
            case "levels": {
                require(request, Feature.MASTER_APPROVAL_ROUTE);
                for (RequestType type : RequestType.values()) {
                    Integer levels = optionalInt(request, "maxLevels_" + type.name());
                    if (levels != null) {
                        masterUseCase.saveApprovalRouteSetting(type, levels);
                    }
                }
                return "組織図でたどる段数を保存しました。";
            }
            case "route": {
                require(request, Feature.MASTER_APPROVAL_ROUTE);
                Integer id = optionalInt(request, "id");
                String typeParam = request.getParameter("requestType");
                RequestType type = typeParam == null || typeParam.isBlank() ? null : RequestType.valueOf(typeParam);
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
                return id == null ? "例外ルートを追加しました。" : "例外ルートを更新しました。";
            }
            case "routeDelete": {
                require(request, Feature.MASTER_APPROVAL_ROUTE);
                masterUseCase.deleteApprovalRoute(requiredInt(request, "id"));
                return "例外ルートを削除しました。";
            }
            default:
                throw new IllegalArgumentException("保存する内容が分かりません。画面を読み込み直してください。");
        }
    }

    private static boolean has(HttpServletRequest request, Feature feature) {
        HttpSession session = request.getSession(false);
        Object features = session != null ? session.getAttribute("features") : null;
        return features instanceof Set && ((Set<?>) features).contains(feature.name());
    }

    private static void require(HttpServletRequest request, Feature feature) {
        if (!has(request, feature)) {
            throw new IllegalArgumentException("「" + feature.getLabel() + "」を変更する権限がありません。");
        }
    }

    /**
     * script 要素の中にそのまま置ける JSON にする。
     * 「</script>」で要素が閉じないよう「</」と、改行扱いになる U+2028/U+2029 をエスケープする。
     */
    static String toScriptJson(Object data) {
        try {
            return JSON.writeValueAsString(data)
                    .replace("</", "<\\/")
                    .replace(" ", "\\u2028")
                    .replace(" ", "\\u2029");
        } catch (Exception e) {
            throw new IllegalStateException("画面のデータを作れませんでした。", e);
        }
    }
}
