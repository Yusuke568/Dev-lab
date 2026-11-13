package com.example.kintai.application.service;

import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeActualCheckPort;
import com.example.kintai.domain.port.out.OvertimeActualCheckPort.State;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.kintai.domain.service.OvertimeFormCalculator.Step;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 残業の実績確認。
 *   始める … 事前申請が承認済みで、実績の確認日がある（勤怠の時間外が申請と合った・残業しなかった）とき。
 *            事前申請と同じ承認ルートの段・候補者で、1段目から確認する。申請者本人だけの段（自動承認など）は確認済みにする
 *   確認   … 今の段の候補者の誰か1人が確認すれば次の段へ。最後の段が確認したら確認済み（CONFIRMED）
 *   やり直す … 実績が合わなくなった・申請し直した（承認済みでなくなった）ら、確認を消す
 */
public class OvertimeActualCheckService {

    private final OvertimeRequestPort requestPort;
    private final ApprovalStepPort approvalStepPort;
    private final OvertimeActualCheckPort checkPort;
    private final LoadAttendanceRecordPort attendancePort;

    public OvertimeActualCheckService(OvertimeRequestPort requestPort, ApprovalStepPort approvalStepPort,
                                      OvertimeActualCheckPort checkPort, LoadAttendanceRecordPort attendancePort) {
        this.requestPort = requestPort;
        this.approvalStepPort = approvalStepPort;
        this.checkPort = checkPort;
        this.attendancePort = attendancePort;
    }

    /** 申請の今の状態に合わせて、実績確認を始める・やめる */
    public void sync(int requestId) {
        Optional<OvertimeRequest> found = requestPort.findById(requestId);
        if (found.isEmpty()) return;
        OvertimeRequest req = found.get();
        boolean ready = req.isApproved() && req.getActualConfirmedOn() != null;
        State state = checkPort.findState(requestId);
        if (!ready) {
            if (state.status() != null) checkPort.reset(requestId);
            return;
        }
        if (state.status() != null) return; // もう始まっている
        List<List<Integer>> levels = levelsOf(requestId);
        int applicant = Integer.parseInt(req.getEmployeeId());
        if (levels.isEmpty()) levels = List.of(List.of(applicant)); // 承認ルートの段がない（自動承認）
        checkPort.start(requestId, levels);
        advanceFrom(requestId, 1, levels, applicant);
    }

    /**
     * 承認者が実績を確認する（まとめて確認できる）。
     * @return 確認できなかった申請の理由（確認できた分は含めない）
     */
    public List<String> confirm(String approverId, Collection<Integer> requestIds) {
        int approver = Integer.parseInt(approverId);
        List<String> errors = new ArrayList<>();
        for (Integer id : requestIds) {
            Optional<OvertimeRequest> found = requestPort.findById(id);
            if (found.isEmpty()) {
                errors.add("申請 " + id + " が見つかりません。");
                continue;
            }
            OvertimeRequest req = found.get();
            State state = checkPort.findState(id);
            if (!OvertimeActualCheckPort.PENDING.equals(state.status()) || state.level() == null) {
                errors.add(req.getTargetDate() + " の実績は確認待ちではありません。");
                continue;
            }
            List<List<Integer>> levels = checkLevels(id);
            int level = state.level();
            if (level > levels.size() || !levels.get(level - 1).contains(approver)) {
                errors.add(req.getTargetDate() + " の実績は、あなたの確認の番ではありません。");
                continue;
            }
            if (!checkPort.check(id, level, approver)) {
                errors.add(req.getTargetDate() + " の実績は、同じ段のほかの人がすでに確認しています。");
                continue;
            }
            advanceFrom(id, level + 1, levels, Integer.parseInt(req.getEmployeeId()));
        }
        return errors;
    }

    /** 承認者の確認待ち（画面に出す行） */
    public List<PendingActualCheck> pendingFor(String approverId) {
        Map<Integer, String> pending = checkPort.findPendingForApprover(Integer.parseInt(approverId));
        Map<Integer, List<Step>> steps = checkPort.findSteps(pending.keySet());
        List<PendingActualCheck> rows = new ArrayList<>();
        pending.forEach((id, name) -> requestPort.findById(id).ifPresent(req -> {
            Optional<AttendanceRecord> record = attendancePort == null ? Optional.empty()
                    : attendancePort.loadByEmployeeAndDate(new EmployeeId(req.getEmployeeId()), req.getTargetDate());
            rows.add(new PendingActualCheck(req, name, record.orElse(null), steps.getOrDefault(id, List.of())));
        }));
        return rows;
    }

    /** level 段目から先へ進める。申請者本人だけの段は確認済みにして飛ばす。全段終われば確認済み */
    private void advanceFrom(int requestId, int level, List<List<Integer>> levels, int applicant) {
        int next = level;
        while (next <= levels.size() && levels.get(next - 1).equals(List.of(applicant))) {
            checkPort.check(requestId, next, applicant);
            next++;
        }
        if (next > levels.size()) {
            checkPort.updateState(requestId, OvertimeActualCheckPort.CONFIRMED, null);
        } else {
            checkPort.updateState(requestId, OvertimeActualCheckPort.PENDING, next);
        }
    }

    /** 事前申請の承認ルートの段（段 → 候補者） */
    private List<List<Integer>> levelsOf(int requestId) {
        TreeMap<Integer, List<Integer>> byLevel = new TreeMap<>();
        for (ApprovalStep s : approvalStepPort.findSteps(RequestType.OVERTIME, requestId)) {
            byLevel.computeIfAbsent(s.getLevel(), k -> new ArrayList<>()).add(s.getApproverStaffId());
        }
        return new ArrayList<>(byLevel.values());
    }

    /** 実績確認の段（段 → 候補者）。確認の段は事前申請の段を写したもの */
    private List<List<Integer>> checkLevels(int requestId) {
        return levelsOf(requestId);
    }

    /** 画面の1行：申請・申請者・その日の勤怠・確認の段 */
    public static class PendingActualCheck {
        private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
        private final OvertimeRequest request;
        private final String employeeName;
        private final AttendanceRecord record;
        private final List<Step> steps;

        PendingActualCheck(OvertimeRequest request, String employeeName, AttendanceRecord record, List<Step> steps) {
            this.request = request;
            this.employeeName = employeeName;
            this.record = record;
            this.steps = steps;
        }

        public OvertimeRequest getRequest() { return request; }
        public String getEmployeeName() { return employeeName; }
        public String getActualStart() {
            return record == null || record.getWorkTime() == null ? "-" : record.getWorkTime().getStartTime().format(HM);
        }
        public String getActualEnd() {
            return record == null || record.getWorkTime() == null ? "-" : record.getWorkTime().getEndTime().format(HM);
        }
        public String getActualOvertime() {
            int m = record == null ? 0 : Math.max(0, record.getOvertimeMinutes());
            return String.format("%02d:%02d", m / 60, m % 60);
        }
        /** 「1. 山田 確認済 11/26 → 2. 椎屋 未確認」 */
        public String getRoute() {
            TreeMap<Integer, List<Step>> byLevel = new TreeMap<>();
            for (Step s : steps) byLevel.computeIfAbsent(s.level(), k -> new ArrayList<>()).add(s);
            List<String> parts = new ArrayList<>();
            byLevel.forEach((level, list) -> {
                Step done = list.stream().filter(Step::approved).findFirst().orElse(null);
                String names = done != null ? done.approverName()
                        : String.join("／", list.stream().map(Step::approverName).toList());
                parts.add(level + ". " + names + (done != null
                        ? " 確認済 " + done.decidedOn().getMonthValue() + "/" + done.decidedOn().getDayOfMonth() : " 未確認"));
            });
            return String.join(" → ", parts);
        }
    }
}
