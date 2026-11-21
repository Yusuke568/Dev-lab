package com.example.kintai.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.kintai.application.service.OvertimeFormImportService.RowResult;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals.Mark;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals.Row;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 時間外申請書（Excel）の承認日の取り込み（申請の承認欄） */
class OvertimeFormImportServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);
    private static final LocalDate APPROVED_ON = LocalDate.of(2026, 11, 6);

    private OvertimeRequest request;
    private final List<String> decided = new ArrayList<>();
    private int advanced;
    private final Steps steps = new Steps();
    private OvertimeFormImportService service;

    @BeforeEach
    void setUp() {
        OvertimeRequestPort requestPort = new OvertimeRequestPort() {
            @Override public Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId employeeId, LocalDate date) {
                return request != null && request.getTargetDate().equals(date) ? Optional.of(request) : Optional.empty();
            }
            @Override public List<OvertimeRequest> findByEmployee(EmployeeId employeeId) { return List.of(); }
            @Override public Optional<OvertimeRequest> findById(int requestId) { return Optional.ofNullable(request); }
            @Override public List<OvertimeRequest> findPendingForApprover(String approverId) { return List.of(); }
            @Override public int insert(EmployeeId employeeId, LocalDate date, int planned, String start, String end, String reason) { return 0; }
            @Override public void resubmit(int requestId, int planned, String start, String end, String reason) { }
            @Override public void decide(int requestId, boolean approve, String decidedBy) { decided.add(decidedBy + "@now"); }
            @Override public void decide(int requestId, boolean approve, String decidedBy, LocalDate decidedOn) {
                decided.add(decidedBy + "@" + decidedOn);
            }
            @Override public void advanceLevel(int requestId) { advanced++; }
            @Override public void updateDecidedOn(int requestId, LocalDate decidedOn) { decided.add("update@" + decidedOn); }
        };
        service = new OvertimeFormImportService(requestPort, new ApprovalWorkflowService(steps, null), null);
    }

    private static List<Mark> marks(LocalDate... dates) {
        List<Mark> list = new ArrayList<>();
        for (LocalDate d : dates) list.add(d == null ? Mark.SKIPPED : new Mark(d, false));
        while (list.size() < 3) list.add(Mark.BLANK);
        return list;
    }

    private static OvertimeFormApprovals form(List<Mark> planned) {
        return new OvertimeFormApprovals("浦山　純平", List.of(new Row(DAY, planned, marks())));
    }

    private void pendingRequest(List<List<Integer>> levels) {
        request = new OvertimeRequest(7, "147", null, DAY, 120, "18:00", "20:00", "対応",
                OvertimeRequest.STATUS_PENDING, null, null, null);
        steps.replaceSteps(RequestType.OVERTIME, 7, levels);
    }

    @Test
    void 課長欄の日付で1段のルートを承認済みにする_承認者は主承認者() {
        pendingRequest(List.of(List.of(401, 402)));

        List<RowResult> preview = service.preview("147", form(marks(APPROVED_ON, null, null)));
        assertEquals("ok", preview.get(0).getState());
        assertTrue(decided.isEmpty()); // 読み込みでは保存しない

        service.apply("147", form(marks(APPROVED_ON, null, null)));
        assertEquals(List.of("401@" + APPROVED_ON), decided);
        assertEquals("APPROVED@" + APPROVED_ON + "@401", steps.decisions.get(0));
    }

    @Test
    void 二段のルートは課長と所属長の欄で順に承認する() {
        pendingRequest(List.of(List.of(401), List.of(500)));
        LocalDate second = LocalDate.of(2026, 11, 9);

        service.apply("147", form(marks(APPROVED_ON, second, null)));

        assertEquals(1, advanced);
        assertEquals(List.of("500@" + second), decided);
    }

    @Test
    void 所属長欄が横線なのにシステムに2段目があれば知らせて止める() {
        pendingRequest(List.of(List.of(401), List.of(500)));

        RowResult r = service.apply("147", form(marks(APPROVED_ON, null, null))).get(0);

        assertEquals("warn", r.getState());
        assertTrue(r.getNotesText().contains("所属長"));
        assertEquals(1, advanced);       // 1段目は承認した
        assertTrue(decided.isEmpty());   // 申請はまだ承認済みにしない
    }

    @Test
    void システムにない申請と却下された申請は取り込まない() {
        request = null;
        assertTrue(service.apply("147", form(marks(APPROVED_ON))).get(0).getNotesText().contains("ありません"));

        request = new OvertimeRequest(7, "147", null, DAY, 120, "18:00", "20:00", "対応",
                OvertimeRequest.STATUS_REJECTED, null, null, null);
        assertEquals("warn", service.apply("147", form(marks(APPROVED_ON))).get(0).getState());
        assertTrue(decided.isEmpty());
    }

    @Test
    void 承認欄に日付がない日は何もしない() {
        pendingRequest(List.of(List.of(401)));
        assertTrue(service.apply("147", form(marks())).isEmpty());
    }

    @Test
    void すでに承認済みの段も時間外申請書の日付に直す() {
        pendingRequest(List.of(List.of(401)));
        steps.recordDecision(RequestType.OVERTIME, 7, 1, 401, ApprovalStep.APPROVED, null, LocalDate.of(2026, 11, 20));
        request = new OvertimeRequest(7, "147", null, DAY, 120, "18:00", "20:00", "対応",
                OvertimeRequest.STATUS_APPROVED, "402", "2026-11-20 00:00", null);
        steps.decisions.clear();

        RowResult preview = service.preview("147", form(marks(APPROVED_ON, null, null))).get(0);
        assertEquals("ok", preview.getState());
        assertTrue(preview.getActionsText().contains("11/20 → 11/6"));
        assertTrue(steps.updated.isEmpty());

        service.apply("147", form(marks(APPROVED_ON, null, null)));
        assertEquals(List.of("1@" + APPROVED_ON), steps.updated);
        assertEquals(List.of("update@" + APPROVED_ON), decided); // 申請の承認日も最後の段の日付に
        // 同じ日付なら何もしない
        assertEquals("none", service.preview("147", form(marks(LocalDate.of(2026, 11, 20), null, null))).get(0).getState());
    }

    /** approval_step の代わり */
    private static class Steps implements ApprovalStepPort {
        final Map<Integer, List<ApprovalStep>> store = new HashMap<>();
        final List<String> decisions = new ArrayList<>();
        final List<String> updated = new ArrayList<>();

        @Override
        public void updateDecidedOn(RequestType requestType, int requestId, int level, LocalDate decidedOn) {
            updated.add(level + "@" + decidedOn);
        }

        @Override
        public void replaceSteps(RequestType requestType, int requestId, List<List<Integer>> levels) {
            List<ApprovalStep> list = new ArrayList<>();
            for (int i = 0; i < levels.size(); i++) {
                for (Integer a : levels.get(i)) list.add(new ApprovalStep(i + 1, a, null, null, null));
            }
            store.put(requestId, list);
        }

        @Override
        public List<ApprovalStep> findSteps(RequestType requestType, int requestId) {
            return store.getOrDefault(requestId, new ArrayList<>());
        }

        @Override
        public Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> ids) {
            Map<Integer, List<ApprovalStep>> map = new HashMap<>();
            for (Integer id : ids) map.put(id, findSteps(requestType, id));
            return map;
        }

        @Override
        public void recordDecision(RequestType requestType, int requestId, int level, int approver, String decision) {
            recordDecision(requestType, requestId, level, approver, decision, null, null);
        }

        @Override
        public void recordDecision(RequestType requestType, int requestId, int level, int approver, String decision,
                                   String comment, LocalDate decidedOn) {
            decisions.add(decision + "@" + decidedOn + "@" + approver);
            List<ApprovalStep> list = findSteps(requestType, requestId);
            for (int i = 0; i < list.size(); i++) {
                ApprovalStep old = list.get(i);
                if (old.getLevel() == level && old.getApproverStaffId() == approver) {
                    list.set(i, new ApprovalStep(level, approver, null, decision, String.valueOf(decidedOn), comment));
                }
            }
        }
    }
}
