package com.example.approval.application.service;

import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalWorkflowServiceTest {

    private InMemorySteps steps;
    private ApprovalWorkflowService service;

    @BeforeEach
    void setUp() {
        steps = new InMemorySteps();
        Map<Integer, Department> org = new HashMap<>();
        org.put(1, new Department(1, "開発部", null, 200, 0));
        org.put(2, new Department(2, "開発1課", 1, 300, 0));
        service = new ApprovalWorkflowService(steps, new OrganizationPort() {
            @Override public Map<Integer, Department> findAllDepartments() { return org; }
            @Override public Integer findDepartmentIdOfStaff(int staffId) { return staffId == 1 ? 2 : null; }
            @Override public int findMaxLevels(RequestType requestType) { return 2; }
        });
    }

    @Test
    void 一段目の承認で次の段へ進み_最終段の承認で承認済みになる() {
        service.startRoute(RequestType.LEAVE, 10, "1");

        assertEquals(ApprovalOutcome.NEXT_LEVEL, service.decide(RequestType.LEAVE, 10, 1, "300", true));
        assertEquals(ApprovalOutcome.APPROVED, service.decide(RequestType.LEAVE, 10, 2, "200", true));
        assertEquals(ApprovalStep.APPROVED, steps.findSteps(RequestType.LEAVE, 10).get(1).getDecision());
    }

    @Test
    void 却下するとその時点で却下になる() {
        service.startRoute(RequestType.OVERTIME, 10, "1");

        assertEquals(ApprovalOutcome.REJECTED, service.decide(RequestType.OVERTIME, 10, 1, "300", false));
    }

    @Test
    void 現在の段の承認者以外は判定できない() {
        service.startRoute(RequestType.LEAVE, 10, "1");

        // 2段目の承認者が、1段目の段を処理しようとしても拒否される
        assertThrows(IllegalStateException.class, () -> service.decide(RequestType.LEAVE, 10, 1, "200", true));
    }

    @Test
    void 同じ段を二重に処理できない() {
        service.startRoute(RequestType.LEAVE, 10, "1");
        service.decide(RequestType.LEAVE, 10, 1, "300", true);

        assertThrows(IllegalStateException.class, () -> service.decide(RequestType.LEAVE, 10, 1, "300", true));
    }

    @Test
    void 再申請するとルートが作り直される() {
        service.startRoute(RequestType.LEAVE, 10, "1");
        service.decide(RequestType.LEAVE, 10, 1, "300", false);

        service.startRoute(RequestType.LEAVE, 10, "1");
        assertEquals(ApprovalOutcome.NEXT_LEVEL, service.decide(RequestType.LEAVE, 10, 1, "300", true));
    }

    @Test
    void 所属部署が無い社員は申請できない() {
        assertThrows(IllegalStateException.class, () -> service.checkRouteAvailable(RequestType.LEAVE, "2"));
    }

    /** approval_step の代わりのメモリ上の実装。 */
    private static class InMemorySteps implements ApprovalStepPort {
        private final Map<String, List<ApprovalStep>> store = new HashMap<>();

        private static String key(RequestType type, int id) {
            return type + ":" + id;
        }

        @Override
        public void replaceSteps(RequestType requestType, int requestId, List<Integer> approverStaffIds) {
            List<ApprovalStep> list = new ArrayList<>();
            for (int i = 0; i < approverStaffIds.size(); i++) {
                list.add(new ApprovalStep(i + 1, approverStaffIds.get(i), null, null, null));
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
        public void recordDecision(RequestType requestType, int requestId, int level, String decision) {
            List<ApprovalStep> list = findSteps(requestType, requestId);
            ApprovalStep old = list.get(level - 1);
            list.set(level - 1, new ApprovalStep(level, old.getApproverStaffId(), null, decision, "2026-11-03 10:00"));
        }
    }
}
