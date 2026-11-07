package com.example.approval.application.service;

import com.example.approval.domain.model.ApprovalLevel;
import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.RouteAssignment;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.approval.domain.port.out.OrganizationPort;
import com.example.master.domain.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalWorkflowServiceTest {

    private InMemorySteps steps;
    private ApprovalWorkflowService service;
    private final Map<Integer, Department> org = new HashMap<>();
    private final Map<Integer, List<Integer>> subApprovers = new HashMap<>();
    private final Map<Integer, List<Integer>> concurrent = new HashMap<>();
    private final List<ApprovalRoute> routes = new ArrayList<>();

    @BeforeEach
    void setUp() {
        steps = new InMemorySteps();
        // 開発部(承認者200) → 開発1課(承認者300)。営業部(承認者400)
        org.put(1, new Department(1, "開発部", null, 200, 0));
        org.put(2, new Department(2, "開発1課", 1, 300, 0));
        org.put(3, new Department(3, "営業部", null, 400, 0));
        service = new ApprovalWorkflowService(steps, new OrganizationPort() {
            @Override public Map<Integer, Department> findAllDepartments() { return org; }
            // 1 は開発1課、200（開発部の承認者＝最上位）は開発部
            @Override public Integer findDepartmentIdOfStaff(int staffId) {
                return staffId == 1 ? Integer.valueOf(2) : staffId == 200 ? Integer.valueOf(1) : null;
            }
            @Override public List<Integer> findDepartmentIdsOfStaff(int staffId) {
                List<Integer> ids = new ArrayList<>();
                Integer primary = findDepartmentIdOfStaff(staffId);
                if (primary != null) ids.add(primary);
                ids.addAll(concurrent.getOrDefault(staffId, List.of()));
                return ids;
            }
            @Override public Map<Integer, List<Integer>> findSubApprovers() { return subApprovers; }
            @Override public List<ApprovalRoute> findApprovalRoutes() { return routes; }
            @Override public int findMaxLevels(RequestType requestType) { return 2; }
        });
    }

    @Test
    void 一段目の承認で次の段へ進み_最終段の承認で承認済みになる() {
        service.startRoute(RequestType.LEAVE, 10, "1");
        assertEquals(ApprovalOutcome.NEXT_LEVEL, service.decide(RequestType.LEAVE, 10, 1, "300", true));
        assertEquals(ApprovalOutcome.APPROVED, service.decide(RequestType.LEAVE, 10, 2, "200", true));
    }

    @Test
    void 副承認者も同じ段の候補になり_どちらか1人の承認で次の段へ進む() {
        subApprovers.put(2, List.of(310));
        service.startRoute(RequestType.OVERTIME, 20, "1");

        assertEquals(ApprovalOutcome.NEXT_LEVEL, service.decide(RequestType.OVERTIME, 20, 1, "310", true));
        // 同じ段の主承認者は、もう判定できない
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.decide(RequestType.OVERTIME, 20, 1, "300", true));
        assertTrue(e.getMessage().contains("ほかの承認者"));

        // 画面表示では、判定した人だけが出る
        List<ApprovalLevel> route = service.getRoutes(RequestType.OVERTIME, List.of(20)).get(20);
        assertEquals("1. 310 承認", route.get(0).getLabel());
        assertEquals("2. 200 未処理", route.get(1).getLabel());
    }

    @Test
    void 候補者でない人は判定できない() {
        service.startRoute(RequestType.OVERTIME, 21, "1");
        assertThrows(IllegalStateException.class, () -> service.decide(RequestType.OVERTIME, 21, 1, "200", true));
    }

    @Test
    void 社員に割り当てた承認ルートで回る() {
        routes.add(new ApprovalRoute(1, "派遣先Aルート", null, null, List.of(List.of(500, 501), List.of(600)),
                List.of(new RouteAssignment(RouteAssignment.STAFF, 1, null))));
        service.startRoute(RequestType.OVERTIME, 30, "1");

        assertEquals(ApprovalOutcome.NEXT_LEVEL, service.decide(RequestType.OVERTIME, 30, 1, "501", true));
        assertEquals(ApprovalOutcome.APPROVED, service.decide(RequestType.OVERTIME, 30, 2, "600", true));
    }

    @Test
    void 兼任部署を選ぶとその部署の承認者に回る() {
        concurrent.put(1, List.of(3));
        service.startRoute(RequestType.OVERTIME, 40, "1", 3);
        assertEquals(ApprovalOutcome.APPROVED, service.decide(RequestType.OVERTIME, 40, 1, "400", true));
    }

    @Test
    void 所属していない部署は選べない() {
        assertThrows(IllegalArgumentException.class, () -> service.checkRouteAvailable(RequestType.OVERTIME, "1", 3));
    }

    @Test
    void 最上位の承認者本人の申請は自動承認として本人の承認を記録する() {
        assertTrue(service.startRoute(RequestType.OVERTIME, 11, "200"));

        List<ApprovalStep> route = steps.findSteps(RequestType.OVERTIME, 11);
        assertEquals(1, route.size());
        assertEquals(200, route.get(0).getApproverStaffId());
        assertTrue(route.get(0).isDecided());
    }

    @Test
    void 承認者がいる申請は自動承認しない() {
        assertFalse(service.startRoute(RequestType.OVERTIME, 12, "1"));
    }

    @Test
    void 所属部署が無い社員は申請できない() {
        assertThrows(IllegalStateException.class, () -> service.checkRouteAvailable(RequestType.LEAVE, "2"));
    }

    /** approval_step の代わりのメモリ上の実装（1段に候補者が複数行）。 */
    private static class InMemorySteps implements ApprovalStepPort {
        private final Map<String, List<ApprovalStep>> store = new HashMap<>();

        private static String key(RequestType type, int id) {
            return type + ":" + id;
        }

        @Override
        public void replaceSteps(RequestType requestType, int requestId, List<List<Integer>> levels) {
            List<ApprovalStep> list = new ArrayList<>();
            for (int i = 0; i < levels.size(); i++) {
                for (Integer approver : levels.get(i)) {
                    list.add(new ApprovalStep(i + 1, approver, null, null, null));
                }
            }
            store.put(key(requestType, requestId), list);
        }

        @Override
        public List<ApprovalStep> findSteps(RequestType requestType, int requestId) {
            return store.getOrDefault(key(requestType, requestId), new ArrayList<>());
        }

        @Override
        public Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> requestIds) {
            Map<Integer, List<ApprovalStep>> map = new HashMap<>();
            for (Integer id : requestIds) {
                map.put(id, findSteps(requestType, id));
            }
            return map;
        }

        @Override
        public void recordDecision(RequestType requestType, int requestId, int level, int approverStaffId, String decision) {
            List<ApprovalStep> list = findSteps(requestType, requestId);
            for (int i = 0; i < list.size(); i++) {
                ApprovalStep old = list.get(i);
                if (old.getLevel() == level && old.getApproverStaffId() == approverStaffId) {
                    list.set(i, new ApprovalStep(level, approverStaffId, null, decision, "2026-11-12 10:00"));
                }
            }
        }
    }
}
