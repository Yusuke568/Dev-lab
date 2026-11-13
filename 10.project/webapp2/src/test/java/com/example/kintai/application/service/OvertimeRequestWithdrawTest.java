package com.example.kintai.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.approval.domain.port.out.OrganizationPort;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.master.domain.model.Department;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 残業事前申請の取り下げ */
class OvertimeRequestWithdrawTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 20);

    /** 申請ID → 申請（取り下げ・再申請で状態が変わる） */
    private final Map<Integer, OvertimeRequest> store = new HashMap<>();
    private final List<Integer> resubmitted = new ArrayList<>();

    private static OvertimeRequest request(int id, String staff, String status) {
        return new OvertimeRequest(id, staff, "", DAY, 120, "09:00", "20:00", "障害対応", status, null, null, "2026-11-10 09:00");
    }

    private final OvertimeRequestPort port = new OvertimeRequestPort() {
        @Override public Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId employeeId, LocalDate date) {
            return store.values().stream().filter(r -> r.getEmployeeId().equals(employeeId.getValue()) && r.getTargetDate().equals(date)).findFirst();
        }
        @Override public List<OvertimeRequest> findByEmployee(EmployeeId employeeId) { return new ArrayList<>(store.values()); }
        @Override public Optional<OvertimeRequest> findById(int requestId) { return Optional.ofNullable(store.get(requestId)); }
        @Override public List<OvertimeRequest> findPendingForApprover(String approverId) { return List.of(); }
        @Override public int insert(EmployeeId employeeId, LocalDate date, int minutes, String start, String end, String reason) { return 99; }
        @Override public void resubmit(int requestId, int minutes, String start, String end, String reason) {
            resubmitted.add(requestId);
            store.put(requestId, request(requestId, store.get(requestId).getEmployeeId(), OvertimeRequest.STATUS_PENDING));
        }
        @Override public void decide(int requestId, boolean approve, String decidedBy) { }
        @Override public void advanceLevel(int requestId) { }
        @Override public boolean withdraw(int requestId) {
            OvertimeRequest r = store.get(requestId);
            if (r == null || !r.isPending()) return false;
            store.put(requestId, request(requestId, r.getEmployeeId(), OvertimeRequest.STATUS_WITHDRAWN));
            return true;
        }
    };

    /** 126 は開発1課（承認者 300） */
    private final OrganizationPort organization = new OrganizationPort() {
        @Override public Map<Integer, Department> findAllDepartments() { return Map.of(2, new Department(2, "開発1課", null, 300, 0)); }
        @Override public Integer findDepartmentIdOfStaff(int staffId) { return 2; }
        @Override public List<Integer> findDepartmentIdsOfStaff(int staffId) { return List.of(2); }
        @Override public Map<Integer, List<Integer>> findSubApprovers() { return Map.of(); }
        @Override public List<ApprovalRoute> findApprovalRoutes() { return List.of(); }
        @Override public int findMaxLevels(RequestType requestType) { return 1; }
    };

    private final ApprovalStepPort steps = new ApprovalStepPort() {
        @Override public void replaceSteps(RequestType requestType, int requestId, List<List<Integer>> levels) { }
        @Override public List<ApprovalStep> findSteps(RequestType requestType, int requestId) { return List.of(); }
        @Override public Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> ids) { return Map.of(); }
        @Override public void recordDecision(RequestType requestType, int requestId, int level, int approverStaffId, String decision) { }
    };

    private final OvertimeRequestService service = new OvertimeRequestService(port, new ApprovalWorkflowService(steps, organization));

    @Test
    void 自分の申請中の申請は取り下げられて同じ日にもう一度申請できる() {
        store.put(1, request(1, "126", OvertimeRequest.STATUS_PENDING));

        OvertimeRequest withdrawn = service.withdraw("126", 1);

        assertEquals(DAY, withdrawn.getTargetDate());
        assertTrue(store.get(1).isWithdrawn());
        assertEquals("取り下げ", store.get(1).getStatusLabel());

        service.submit("126", DAY, "09:00", "19:00", "直した理由");
        assertEquals(List.of(1), resubmitted, "取り下げた申請を使って申請し直す");
        assertTrue(store.get(1).isPending());
    }

    @Test
    void ほかの人の申請は取り下げられない() {
        store.put(1, request(1, "126", OvertimeRequest.STATUS_PENDING));
        assertThrows(IllegalArgumentException.class, () -> service.withdraw("127", 1));
        assertTrue(store.get(1).isPending());
    }

    @Test
    void 承認済みや却下済みの申請は取り下げられない() {
        store.put(1, request(1, "126", OvertimeRequest.STATUS_APPROVED));
        store.put(2, request(2, "126", OvertimeRequest.STATUS_REJECTED));
        assertThrows(IllegalStateException.class, () -> service.withdraw("126", 1));
        assertThrows(IllegalStateException.class, () -> service.withdraw("126", 2));
    }

    @Test
    void 申請中のまま同じ日に申請すると_同じ申請を申請し直す() {
        store.put(1, request(1, "126", OvertimeRequest.STATUS_PENDING));
        service.submit("126", DAY, "09:00", "19:00", "訂正");
        assertEquals(List.of(1), resubmitted, "新しい申請は作らず、同じ申請を書き換える");
        assertTrue(store.get(1).isPending());
    }
}
