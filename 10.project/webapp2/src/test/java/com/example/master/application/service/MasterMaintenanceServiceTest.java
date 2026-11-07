package com.example.master.application.service;

import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.RouteAssignment;
import com.example.master.domain.model.Department;
import com.example.master.domain.port.out.MasterPort;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MasterMaintenanceServiceTest {

    @Test
    void 部署を親の直後に子が来る順に並べて深さを付ける() {
        List<Department> ordered = MasterMaintenanceService.orderAsTree(List.of(
                new Department(3, "開発1課", 2, null, 0),
                new Department(1, "本部", null, null, 0),
                new Department(4, "営業部", 1, null, 1),
                new Department(2, "開発部", 1, null, 0)));

        assertEquals(List.of("本部", "開発部", "開発1課", "営業部"),
                ordered.stream().map(Department::getName).collect(Collectors.toList()));
        assertEquals(List.of(0, 1, 2, 1),
                ordered.stream().map(Department::getDepth).collect(Collectors.toList()));
    }

    @Test
    void 輪になった部署も一覧から消えない() {
        List<Department> ordered = MasterMaintenanceService.orderAsTree(List.of(
                new Department(1, "A", 2, null, 0),
                new Department(2, "B", 1, null, 0)));

        assertEquals(2, ordered.size());
    }

    // ---- 承認ルートの割り当て ----

    private final List<ApprovalRoute> routes = List.of(
            new ApprovalRoute(1, "共通", null, null, List.of(List.of(300)), List.of()),
            new ApprovalRoute(2, "休暇用", RequestType.LEAVE, null, List.of(List.of(400)), List.of()));
    private final List<Object[]> replaced = new ArrayList<>();

    /** findApprovalRoutes / replaceRouteAssignments だけを持つ MasterPort */
    private MasterPort masterPort() {
        return (MasterPort) Proxy.newProxyInstance(MasterPort.class.getClassLoader(), new Class<?>[] {MasterPort.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findApprovalRoutes": return routes;
                        case "replaceRouteAssignments": replaced.add(args); return null;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    @Test
    void 部署に残業と休暇のルートを割り当てる() {
        new MasterMaintenanceService(masterPort()).assignDepartmentRoutes(5, 1, 2);

        assertEquals(1, replaced.size());
        assertEquals(RouteAssignment.DEPARTMENT, replaced.get(0)[0]);
        assertEquals(5, replaced.get(0)[1]);
        assertEquals(List.of(1, 2), replaced.get(0)[2]);
    }

    @Test
    void 残業と休暇で同じ両方用のルートなら割り当ては1件() {
        new MasterMaintenanceService(masterPort()).assignStaffRoutes(126, 1, 1);

        assertEquals(RouteAssignment.STAFF, replaced.get(0)[0]);
        assertEquals(List.of(1), replaced.get(0)[2]);
    }

    @Test
    void 休暇用のルートは残業には割り当てられない() {
        MasterMaintenanceService service = new MasterMaintenanceService(masterPort());
        assertThrows(IllegalArgumentException.class, () -> service.assignDepartmentRoutes(5, 2, null));
    }

    // ---- 行の削除 ----

    private final List<String> deleted = new ArrayList<>();

    /** 使われている数を返し、削除を記録する MasterPort */
    private MasterPort usagePort(int count, int[] deptUsage) {
        return (MasterPort) Proxy.newProxyInstance(MasterPort.class.getClassLoader(), new Class<?>[] {MasterPort.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("count")) {
                        return name.equals("countDepartmentUsage") ? deptUsage : count;
                    }
                    if (name.startsWith("delete")) {
                        deleted.add(name + ":" + args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(name);
                });
    }

    @Test
    void 使われていない役職は削除できる() {
        new MasterMaintenanceService(usagePort(0, null)).deleteJobClass(3);
        assertEquals(List.of("deleteJobClass:3"), deleted);
    }

    @Test
    void 社員がいる役職は削除しない() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new MasterMaintenanceService(usagePort(2, null)).deleteJobClass(3));
        assertEquals("この役職の社員が2人います。社員の役職を変えてから削除してください。", e.getMessage());
        assertEquals(List.of(), deleted);
    }

    @Test
    void 使われている勤務区分と権限グループは削除しない() {
        MasterMaintenanceService service = new MasterMaintenanceService(usagePort(5, null));
        assertThrows(IllegalStateException.class, () -> service.deleteAbstract(1));
        assertThrows(IllegalStateException.class, () -> service.deletePermissionGroup(1));
        assertEquals(List.of(), deleted);
    }

    @Test
    void 部署は下の部署や社員がいれば理由を出して削除しない() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new MasterMaintenanceService(usagePort(0, new int[] {1, 3, 0})).deleteDepartment(2));
        assertEquals("下の部署が1件、所属している社員が3人あるため削除できません。移してから削除してください。", e.getMessage());

        new MasterMaintenanceService(usagePort(0, new int[] {0, 0, 0})).deleteDepartment(2);
        assertEquals(List.of("deleteDepartment:2"), deleted);
    }
}
