package com.example.kintai.application.service;

import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeActualCheckPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.kintai.domain.service.OvertimeFormCalculator.Step;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 残業の実績確認（承認ルートの順に各段が確認する） */
class OvertimeActualCheckServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 20);
    private static final LocalDate INPUT = LocalDate.of(2026, 11, 21);

    private OvertimeRequest request;
    /** 事前申請の承認ルート（段 → 候補者） */
    private List<List<Integer>> route = List.of(List.of(300), List.of(400, 401));

    private final OvertimeRequestPort requestPort = new OvertimeRequestPort() {
        @Override public Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId e, LocalDate d) { return Optional.ofNullable(request); }
        @Override public List<OvertimeRequest> findByEmployee(EmployeeId e) { return List.of(); }
        @Override public Optional<OvertimeRequest> findById(int id) { return Optional.ofNullable(request); }
        @Override public List<OvertimeRequest> findPendingForApprover(String a) { return List.of(); }
        @Override public int insert(EmployeeId e, LocalDate d, int m, String s, String t, String r) { return 1; }
        @Override public void resubmit(int id, int m, String s, String t, String r) { }
        @Override public void decide(int id, boolean approve, String by) { }
        @Override public void advanceLevel(int id) { }
    };

    private final ApprovalStepPort stepPort = new ApprovalStepPort() {
        @Override public void replaceSteps(RequestType t, int id, List<List<Integer>> levels) { }
        @Override public List<ApprovalStep> findSteps(RequestType t, int id) {
            List<ApprovalStep> steps = new ArrayList<>();
            for (int i = 0; i < route.size(); i++) {
                for (Integer s : route.get(i)) steps.add(new ApprovalStep(i + 1, s, "社員" + s, "APPROVED", "2026-11-20 10:00"));
            }
            return steps;
        }
        @Override public Map<Integer, List<ApprovalStep>> findSteps(RequestType t, Collection<Integer> ids) { return Map.of(); }
        @Override public void recordDecision(RequestType t, int id, int level, int approver, String decision) { }
    };

    /** 実績確認の保存先（メモリ） */
    private final Map<Integer, List<List<Integer>>> checkLevels = new HashMap<>();
    private final Map<String, LocalDate> checked = new LinkedHashMap<>(); // "段:確認者" → 日
    private OvertimeActualCheckPort.State state = OvertimeActualCheckPort.State.NONE;
    private final Map<Integer, String> comments = new HashMap<>(); // 段 → コメント

    private final OvertimeActualCheckPort checkPort = new OvertimeActualCheckPort() {
        @Override public State findState(int id) { return state; }
        @Override public void start(int id, List<List<Integer>> levels) { checkLevels.put(id, levels); checked.clear(); state = new State(PENDING, 1); }
        @Override public void reset(int id) { checkLevels.remove(id); checked.clear(); state = State.NONE; }
        @Override public boolean check(int id, int level, int approver) {
            if (checked.keySet().stream().anyMatch(k -> k.startsWith(level + ":"))) return false;
            checked.put(level + ":" + approver, INPUT);
            return true;
        }
        @Override public boolean check(int id, int level, int approver, String comment) {
            if (comment != null) comments.put(level, comment);
            return check(id, level, approver);
        }
        @Override public void updateState(int id, String status, Integer level) { state = new State(status, level); }
        @Override public Map<Integer, String> findPendingForApprover(int a) { return Map.of(); }
        @Override public Map<Integer, List<Step>> findSteps(Collection<Integer> ids) { return Map.of(); }
    };

    private final OvertimeActualCheckService service = new OvertimeActualCheckService(requestPort, stepPort, checkPort, null);

    private void approvedRequest(LocalDate confirmedOn) {
        request = new OvertimeRequest(7, "126", null, DAY, 120, null, "20:00", "対応",
                OvertimeRequest.STATUS_APPROVED, "401", "2026-11-20 11:00", "2026-11-20 09:00").withActual(confirmedOn, 2);
    }

    @Test
    void 承認済みで実績が合ったら_承認ルートの1段目から確認待ちになる() {
        approvedRequest(INPUT);
        service.sync(7);
        assertEquals(route, checkLevels.get(7));
        assertEquals(new OvertimeActualCheckPort.State("PENDING", 1), state);
    }

    @Test
    void 実績がまだ合っていない_申請がまだ承認されていないなら始めない() {
        approvedRequest(null);
        service.sync(7);
        assertEquals(OvertimeActualCheckPort.State.NONE, state);

        request = new OvertimeRequest(7, "126", null, DAY, 120, null, "20:00", "対応",
                OvertimeRequest.STATUS_PENDING, null, null, "2026-11-20 09:00").withActual(INPUT, 2);
        service.sync(7);
        assertEquals(OvertimeActualCheckPort.State.NONE, state);
    }

    @Test
    void 段ごとに誰か1人が確認すれば次の段へ_最後の段で確認済み() {
        approvedRequest(INPUT);
        service.sync(7);

        assertTrue(service.confirm("300", List.of(7)).isEmpty());
        assertEquals(new OvertimeActualCheckPort.State("PENDING", 2), state);
        // 2段目は副承認者（401）が確認しても終わる
        assertTrue(service.confirm("401", List.of(7)).isEmpty());
        assertEquals(new OvertimeActualCheckPort.State("CONFIRMED", null), state);
    }

    @Test
    void 自分の番でない人は確認できない() {
        approvedRequest(INPUT);
        service.sync(7);
        List<String> errors = service.confirm("400", List.of(7)); // まだ1段目
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("番ではありません"));
        assertEquals(new OvertimeActualCheckPort.State("PENDING", 1), state);
    }

    @Test
    void 実績が合わなくなったら確認をやめる() {
        approvedRequest(INPUT);
        service.sync(7);
        service.confirm("300", List.of(7));
        approvedRequest(null); // 勤怠を直して合わなくなった
        service.sync(7);
        assertEquals(OvertimeActualCheckPort.State.NONE, state);
        assertTrue(checked.isEmpty());
    }

    @Test
    void 自動承認の申請は申請者本人の確認で終わる() {
        route = List.of(List.of(126));
        approvedRequest(INPUT);
        service.sync(7);
        assertEquals(new OvertimeActualCheckPort.State("CONFIRMED", null), state);
        assertTrue(checked.containsKey("1:126"));
    }

    @Test
    void 確認のときのコメント_任意_を段ごとに残す_空なら残さない() {
        approvedRequest(INPUT);
        service.sync(7);
        assertTrue(service.confirm("300", List.of(7), Map.of(7, "  深夜の対応、了解です ")).isEmpty());
        assertTrue(service.confirm("401", List.of(7), Map.of(7, "   ")).isEmpty());
        assertEquals(Map.of(1, "深夜の対応、了解です"), comments);
    }

    @Test
    void 実績確認の進みにコメントを出す() {
        String route = OvertimeActualCheckService.formatRoute(List.of(
                new Step(1, "山田", true, LocalDate.of(2026, 11, 26), "OK"),
                new Step(2, "椎屋", false, null),
                new Step(2, "佐藤", false, null)));
        assertEquals("1. 山田 確認済 11/26「OK」 → 2. 椎屋／佐藤 未確認", route);
    }
}
