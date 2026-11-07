package com.example.master.application.service;

import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.RouteAssignment;
import com.example.entity.WorkType;
import com.example.leave.domain.model.LeaveGrantRule;
import com.example.master.application.port.in.MasterMaintenanceUseCase;
import com.example.master.domain.model.ApprovalRouteSetting;
import com.example.master.domain.model.Department;
import com.example.master.domain.model.JobClass;
import com.example.master.domain.model.PermissionGroup;
import com.example.master.domain.model.StaffOption;
import com.example.master.domain.port.out.MasterPort;
import com.example.permission.domain.model.Feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * マスタ管理画面のユースケース実装。
 */
public class MasterMaintenanceService implements MasterMaintenanceUseCase {

    private final MasterPort masterPort;

    public MasterMaintenanceService(MasterPort masterPort) {
        this.masterPort = masterPort;
    }

    // ------------------------------------------------------------------ 役職

    @Override
    public List<JobClass> getJobClasses() {
        return masterPort.findJobClasses();
    }

    @Override
    public void saveJobClass(Integer id, String name, Integer permissionGroupId) {
        masterPort.saveJobClass(id, requireName(name, "役職名"), permissionGroupId);
    }

    // ------------------------------------------------------------------ 権限グループ

    @Override
    public List<PermissionGroup> getPermissionGroups() {
        return masterPort.findPermissionGroups();
    }

    @Override
    public void savePermissionGroup(Integer id, String name, Set<String> featureCodes) {
        Set<String> valid = new LinkedHashSet<>();
        for (String code : featureCodes) {
            valid.add(Feature.valueOf(code).name());
        }
        // 権限グループを設定できる人がいなくなると、画面から権限を戻せなくなるので止める
        if (id != null && !valid.contains(Feature.MASTER_PERMISSION_GROUP.name())) {
            boolean otherGroupCanManage = masterPort.findPermissionGroups().stream()
                    .filter(g -> g.getId() != id)
                    .anyMatch(g -> g.getFeatureCodes().contains(Feature.MASTER_PERMISSION_GROUP.name())
                            || g.getFeatureCodes().contains(Feature.LEGACY_MASTER_ADMIN));
            if (!otherGroupCanManage) {
                throw new IllegalArgumentException("「" + Feature.MASTER_PERMISSION_GROUP.getLabel()
                        + "」を使えるグループが1つもなくなるため、このグループから外せません。先にほかのグループに付けてください。");
            }
        }
        int groupId = masterPort.savePermissionGroup(id, requireName(name, "権限グループ名"));
        masterPort.replaceGroupFeatures(groupId, valid);
    }

    // ------------------------------------------------------------------ 組織図

    @Override
    public List<Department> getDepartmentTree() {
        return orderAsTree(masterPort.findDepartments());
    }

    @Override
    public void saveDepartment(Integer id, String name, Integer parentId, Integer approverStaffId,
                               List<Integer> subApproverIds, int sortOrder) {
        String validName = requireName(name, "部署名");
        if (id != null && parentId != null) {
            Map<Integer, Department> byId = new HashMap<>();
            for (Department d : masterPort.findDepartments()) {
                byId.put(d.getId(), d);
            }
            // 親を自分自身や自分の下位部署にすると、ツリーが輪になってしまう
            Set<Integer> visited = new HashSet<>();
            Integer current = parentId;
            while (current != null && visited.add(current)) {
                if (current.equals(id)) {
                    throw new IllegalArgumentException("上位部署に、自分自身または自分の下位部署は指定できません。");
                }
                Department d = byId.get(current);
                current = d != null ? d.getParentId() : null;
            }
        }
        int savedId = masterPort.saveDepartment(id, validName, parentId, approverStaffId, sortOrder);
        if (subApproverIds != null) {
            // 主承認者と同じ人は副承認者に入れない
            List<Integer> subs = new ArrayList<>(new LinkedHashSet<>(subApproverIds));
            subs.removeIf(s -> s == null || s.equals(approverStaffId));
            masterPort.replaceSubApprovers(savedId, subs);
        }
    }

    @Override
    public Map<Integer, List<Integer>> getSubApprovers() {
        return masterPort.findSubApprovers();
    }

    @Override
    public List<Integer> getConcurrentDepartmentIds(int staffId) {
        return masterPort.findConcurrentDepartmentIds(staffId);
    }

    @Override
    public Map<Integer, List<Integer>> getAllConcurrentDepartments() {
        return masterPort.findAllConcurrentDepartments();
    }

    @Override
    public void assignConcurrentDepartments(int staffId, List<Integer> departmentIds) {
        Integer primary = masterPort.findDepartmentIdOfStaff(staffId);
        List<Integer> ids = new ArrayList<>(new LinkedHashSet<>(departmentIds));
        ids.removeIf(d -> d == null || d.equals(primary));
        masterPort.replaceConcurrentDepartments(staffId, ids);
    }

    // ------------------------------------------------------------------ 承認ルートマスタ

    @Override
    public List<ApprovalRoute> getApprovalRoutes() {
        return masterPort.findApprovalRoutes();
    }

    @Override
    public void saveApprovalRoute(Integer id, String name, RequestType requestType, String note,
                                  List<List<Integer>> levels, List<RouteAssignment> assignments) {
        String validName = requireName(name, "ルート名");
        // 空の段を詰め、同じ段の重複を除く
        List<List<Integer>> validLevels = new ArrayList<>();
        for (List<Integer> level : levels) {
            List<Integer> approvers = new ArrayList<>(new LinkedHashSet<>(level));
            approvers.removeIf(Objects::isNull);
            if (!approvers.isEmpty()) {
                validLevels.add(approvers);
            }
        }
        if (validLevels.isEmpty()) {
            throw new IllegalArgumentException("承認者を1人以上選んでください。");
        }
        // 同じ申請種別で同じ社員・部署を別のルートにも割り当てると、どちらを使うか分からなくなる
        for (ApprovalRoute other : masterPort.findApprovalRoutes()) {
            if (other.getId() == null || other.getId().equals(id)) {
                continue;
            }
            boolean typesOverlap = other.getRequestType() == null || requestType == null
                    || other.getRequestType() == requestType;
            if (!typesOverlap) {
                continue;
            }
            for (RouteAssignment a : assignments) {
                if (other.isAssignedTo(a.getTargetType(), a.getTargetId())) {
                    String target = a.isStaff() ? "社員 " + a.getTargetId() : "部署（ID " + a.getTargetId() + "）";
                    throw new IllegalArgumentException(target + " は、すでに承認ルート「" + other.getName()
                            + "」に割り当てています。そちらの割り当てを外してから保存してください。");
                }
            }
        }
        masterPort.saveApprovalRoute(new ApprovalRoute(id, validName, requestType,
                note == null || note.isBlank() ? null : note.strip(), validLevels, assignments));
    }

    @Override
    public void deleteApprovalRoute(int id) {
        masterPort.deleteApprovalRoute(id);
    }

    @Override
    public void assignStaffRoutes(int staffId, Integer overtimeRouteId, Integer leaveRouteId) {
        List<ApprovalRoute> routes = masterPort.findApprovalRoutes();
        checkRouteFor(routes, overtimeRouteId, RequestType.OVERTIME);
        checkRouteFor(routes, leaveRouteId, RequestType.LEAVE);
        // 残業と休暇で同じルート（種別「両方」）なら1件の割り当てにする
        List<Integer> routeIds = new ArrayList<>();
        if (overtimeRouteId != null) {
            routeIds.add(overtimeRouteId);
        }
        if (leaveRouteId != null && !routeIds.contains(leaveRouteId)) {
            routeIds.add(leaveRouteId);
        }
        masterPort.replaceStaffRouteAssignments(staffId, routeIds);
    }

    private static void checkRouteFor(List<ApprovalRoute> routes, Integer routeId, RequestType type) {
        if (routeId == null) {
            return;
        }
        ApprovalRoute route = routes.stream().filter(r -> routeId.equals(r.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("承認ルートが見つかりません。画面を読み込み直してください。"));
        if (!route.appliesTo(type)) {
            throw new IllegalArgumentException("承認ルート「" + route.getName() + "」は" + type.getLabel() + "には使えません。");
        }
    }

    @Override
    public List<StaffOption> getStaffOptions() {
        return masterPort.findStaffOptions();
    }

    @Override
    public void assignDepartment(int staffId, Integer departmentId) {
        masterPort.assignDepartment(staffId, departmentId);
    }

    @Override
    public Integer getDepartmentIdOfStaff(int staffId) {
        return masterPort.findDepartmentIdOfStaff(staffId);
    }

    // ------------------------------------------------------------------ 適用マスタ

    @Override
    public List<WorkType> getAbstracts() {
        return masterPort.findAbstracts();
    }

    @Override
    public void saveAbstract(boolean isNew, int id, String name, boolean isWork, boolean isPaid,
                             boolean isLegalHoliday, boolean isPrescribedHoliday, double paidLeaveDays) {
        String validName = requireName(name, "区分名");
        if (paidLeaveDays != 0 && paidLeaveDays != 0.5 && paidLeaveDays != 1) {
            throw new IllegalArgumentException("有給消費日数は 0・0.5・1 のどれかにしてください。");
        }
        if (isNew) {
            if (masterPort.existsAbstract(id)) {
                throw new IllegalArgumentException("ID " + id + " はすでに使われています。");
            }
            masterPort.insertAbstract(id, validName, isWork, isPaid, isLegalHoliday, isPrescribedHoliday, paidLeaveDays);
        } else {
            masterPort.updateAbstract(id, validName, isWork, isPaid, isLegalHoliday, isPrescribedHoliday, paidLeaveDays);
        }
    }

    // ------------------------------------------------------------------ 有給付与ルール

    @Override
    public List<LeaveGrantRule> getLeaveGrantRules() {
        return masterPort.findLeaveGrantRules();
    }

    @Override
    public void saveLeaveGrantRule(int yearsOfService, int grantDays) {
        if (yearsOfService < 0 || grantDays < 0) {
            throw new IllegalArgumentException("勤続年数と付与日数は0以上で入力してください。");
        }
        masterPort.saveLeaveGrantRule(yearsOfService, grantDays);
    }

    @Override
    public void deleteLeaveGrantRule(int yearsOfService) {
        masterPort.deleteLeaveGrantRule(yearsOfService);
    }

    // ------------------------------------------------------------------ 承認ルート設定

    @Override
    public List<ApprovalRouteSetting> getApprovalRouteSettings() {
        return masterPort.findApprovalRouteSettings();
    }

    @Override
    public void saveApprovalRouteSetting(RequestType requestType, int maxLevels) {
        if (maxLevels < 1 || maxLevels > 10) {
            throw new IllegalArgumentException("承認段数は1〜10で入力してください。");
        }
        masterPort.saveApprovalRouteSetting(requestType, maxLevels);
    }

    // ------------------------------------------------------------------ 会社休日

    @Override
    public List<com.example.master.domain.model.CompanyHoliday> getCompanyHolidays(int year) {
        return masterPort.findCompanyHolidays(year);
    }

    @Override
    public int addCompanyHolidays(java.time.LocalDate from, java.time.LocalDate to, String name) {
        String validName = requireName(name, "休日の名前");
        if (from == null) {
            throw new IllegalArgumentException("日付を入力してください。");
        }
        java.time.LocalDate end = to == null ? from : to;
        if (end.isBefore(from)) {
            throw new IllegalArgumentException("終了日は開始日以降の日付にしてください。");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(from, end) >= 31) {
            throw new IllegalArgumentException("一度に登録できるのは31日までです。");
        }
        int count = 0;
        for (java.time.LocalDate d = from; !d.isAfter(end); d = d.plusDays(1)) {
            masterPort.saveCompanyHoliday(d, validName);
            count++;
        }
        return count;
    }

    @Override
    public void deleteCompanyHoliday(java.time.LocalDate date) {
        masterPort.deleteCompanyHoliday(date);
    }

    // ------------------------------------------------------------------ helpers

    private static String requireName(String name, String label) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(label + "を入力してください。");
        }
        return name.trim();
    }

    /**
     * 親の直後にその子が並ぶ順（深さ優先）に並べ、depth を設定する。
     * 親が見つからない部署はトップレベルとして扱う。
     */
    static List<Department> orderAsTree(List<Department> unsorted) {
        List<Department> departments = new ArrayList<>(unsorted);
        departments.sort(Comparator.comparingInt(Department::getSortOrder).thenComparingInt(Department::getId));
        Map<Integer, Department> byId = new HashMap<>();
        for (Department d : departments) {
            byId.put(d.getId(), d);
        }
        Map<Integer, List<Department>> children = new HashMap<>();
        List<Department> roots = new ArrayList<>();
        for (Department d : departments) {
            if (d.getParentId() == null || !byId.containsKey(d.getParentId()) || Objects.equals(d.getParentId(), d.getId())) {
                roots.add(d);
            } else {
                children.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d);
            }
        }
        List<Department> ordered = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        for (Department root : roots) {
            appendTree(root, 0, children, ordered, visited);
        }
        // 輪になっていて根から届かない部署も、表示から消えないよう末尾に出す
        for (Department d : departments) {
            if (!visited.contains(d.getId())) {
                appendTree(d, 0, children, ordered, visited);
            }
        }
        return ordered;
    }

    private static void appendTree(Department dept, int depth, Map<Integer, List<Department>> children,
                                   List<Department> ordered, Set<Integer> visited) {
        if (!visited.add(dept.getId())) {
            return;
        }
        dept.setDepth(depth);
        ordered.add(dept);
        for (Department child : children.getOrDefault(dept.getId(), new ArrayList<>())) {
            appendTree(child, depth + 1, children, ordered, visited);
        }
    }
}
