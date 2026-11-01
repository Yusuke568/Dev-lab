package com.example.master.application.service;

import com.example.approval.domain.model.RequestType;
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
        int groupId = masterPort.savePermissionGroup(id, requireName(name, "権限グループ名"));
        masterPort.replaceGroupFeatures(groupId, valid);
    }

    // ------------------------------------------------------------------ 組織図

    @Override
    public List<Department> getDepartmentTree() {
        return orderAsTree(masterPort.findDepartments());
    }

    @Override
    public void saveDepartment(Integer id, String name, Integer parentId, Integer approverStaffId, int sortOrder) {
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
        masterPort.saveDepartment(id, validName, parentId, approverStaffId, sortOrder);
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
                             boolean isLegalHoliday, boolean isPrescribedHoliday) {
        String validName = requireName(name, "区分名");
        if (isNew) {
            if (masterPort.existsAbstract(id)) {
                throw new IllegalArgumentException("ID " + id + " はすでに使われています。");
            }
            masterPort.insertAbstract(id, validName, isWork, isPaid, isLegalHoliday, isPrescribedHoliday);
        } else {
            masterPort.updateAbstract(id, validName, isWork, isPaid, isLegalHoliday, isPrescribedHoliday);
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
