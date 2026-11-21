package com.example.kintai.application.service;

import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalLevel;
import com.example.approval.domain.model.RequestType;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals.Mark;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals.Row;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeActualCheckPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 時間外申請書（Excel）の承認日を、勤怠システムの残業事前申請・残業の実績確認に取り込む。
 *
 *   申請の承認欄 … システムの申請が申請中なら、今の段から順に、その段の欄に日付があればその日で承認する
 *                  （1段目→課長、2段目→所属長、3段目以降→社長の欄）。最後の段まで承認したら申請を承認済みにする
 *   実績報告の承認欄 … 実績確認が確認待ちなら、今の段から順に、その段の欄に日付があればその日で確認済みにする
 *   すでにシステムで承認・確認した段も、時間外申請書の日付と違えば、その日付に直す（判定した人はそのまま）
 * 承認者・確認者は、その段の主承認者（最初の候補者）として記録し、理由に「時間外申請書から取込」と残す。
 * 却下・取り下げの申請、システムにない申請は取り込まずに知らせる。
 */
public class OvertimeFormImportService {

    static final String NOTE = "時間外申請書から取込";

    private final OvertimeRequestPort requestPort;
    private final ApprovalWorkflowService workflow;
    private final OvertimeActualCheckService actualCheck;

    public OvertimeFormImportService(OvertimeRequestPort requestPort, ApprovalWorkflowService workflow,
                                     OvertimeActualCheckService actualCheck) {
        this.requestPort = requestPort;
        this.workflow = workflow;
        this.actualCheck = actualCheck;
    }

    /** 1日分の結果。state は ok（取り込む・取り込んだ）／none（変わらない）／warn（取り込めないものがある） */
    public record RowResult(LocalDate date, String plannedLabel, String actualLabel, String systemStatus,
                            List<String> actions, List<String> notes) implements Serializable {
        public String getState() {
            if (!notes.isEmpty()) return "warn";
            return actions.isEmpty() ? "none" : "ok";
        }
        public String getActionsText() { return String.join("、", actions); }
        public String getNotesText() { return String.join(" ", notes); }
        public String getDateLabel() { return date.getMonthValue() + "/" + date.getDayOfMonth(); }
        public LocalDate getDate() { return date; }
        public String getPlannedLabel() { return plannedLabel; }
        public String getActualLabel() { return actualLabel; }
        public String getSystemStatus() { return systemStatus; }
    }

    /** 取り込んだら何が変わるかを見る（保存しない） */
    public List<RowResult> preview(String staffId, OvertimeFormApprovals form) {
        return process(staffId, form, false);
    }

    /** 取り込む */
    public List<RowResult> apply(String staffId, OvertimeFormApprovals form) {
        return process(staffId, form, true);
    }

    private List<RowResult> process(String staffId, OvertimeFormApprovals form, boolean apply) {
        List<RowResult> results = new ArrayList<>();
        for (Row row : form.rows()) {
            if (!row.hasAnyDate()) continue; // 承認欄に日付がない行は何もしない
            results.add(processRow(staffId, row, apply));
        }
        return results;
    }

    private RowResult processRow(String staffId, Row row, boolean apply) {
        List<String> actions = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        Optional<OvertimeRequest> found = requestPort.findByEmployeeAndDate(new EmployeeId(staffId), row.date());
        if (found.isEmpty()) {
            notes.add("システムにこの日の残業事前申請がありません。");
            return new RowResult(row.date(), row.getPlannedLabel(), row.getActualLabel(), "申請なし", actions, notes);
        }
        OvertimeRequest req = found.get();
        int id = req.getId();
        String status = req.getStatusLabel();
        if (!req.isPending() && !req.isApproved()) {
            notes.add("システムでは「" + status + "」なので取り込みません。");
            return new RowResult(row.date(), row.getPlannedLabel(), row.getActualLabel(), status, actions, notes);
        }
        List<ApprovalLevel> route = workflow.getRoutes(RequestType.OVERTIME, List.of(id)).getOrDefault(id, List.of());
        int maxLevel = route.size();

        // 申請の承認欄：すでに承認した段は、承認日を時間外申請書の日付に直す
        for (ApprovalLevel lv : route) {
            if (!lv.isDecided() || !"APPROVED".equals(lv.getDecision())) continue;
            Mark mark = markFor(lv.getLevel(), row.planned());
            LocalDate current = dateOf(lv.getDecided().getDecidedAt());
            if (!mark.hasDate() || mark.date().equals(current)) continue;
            if (apply) workflow.updateDecidedOn(RequestType.OVERTIME, id, lv.getLevel(), mark.date());
            actions.add("申請の" + lv.getLevel() + "段目（" + columnName(lv.getLevel()) + "）の承認日を "
                    + label(current) + " → " + mark.getLabel() + " に直す");
        }
        // 承認済みの申請は、申請の承認日を最後の段の日付にそろえる
        if (apply && req.isApproved() && maxLevel > 0 && markFor(maxLevel, row.planned()).hasDate()) {
            requestPort.updateDecidedOn(id, markFor(maxLevel, row.planned()).date());
        }

        // 申請の承認欄：申請中なら、今の段から順に承認する
        boolean approved = req.isApproved();
        if (req.isPending()) {
            if (maxLevel == 0) {
                notes.add("システムの申請に承認ルートがありません。");
            } else {
                for (int level = req.getCurrentLevel(); level <= maxLevel; level++) {
                    Mark mark = markFor(level, row.planned());
                    String col = columnName(level);
                    if (!mark.hasDate()) {
                        notes.add(mark.skipped()
                                ? "申請の" + col + "欄が「－」ですが、システムの承認ルートには" + level + "段目があります。"
                                : "申請の" + col + "欄（" + level + "段目）の承認日がまだありません。");
                        break;
                    }
                    ApprovalOutcome outcome;
                    if (apply) {
                        outcome = workflow.approveOn(RequestType.OVERTIME, id, level, mark.date(), NOTE);
                        if (outcome == ApprovalOutcome.NEXT_LEVEL) {
                            requestPort.advanceLevel(id);
                        } else {
                            Integer approver = workflow.mainApprover(RequestType.OVERTIME, id, level);
                            requestPort.decide(id, true, String.valueOf(approver), mark.date());
                            if (actualCheck != null) actualCheck.sync(id);
                        }
                    } else {
                        outcome = level < maxLevel ? ApprovalOutcome.NEXT_LEVEL : ApprovalOutcome.APPROVED;
                    }
                    actions.add("申請の" + level + "段目（" + col + "）を " + mark.getLabel() + " に承認");
                    if (outcome == ApprovalOutcome.APPROVED) {
                        approved = true;
                        break;
                    }
                }
            }
        }

        // 実績報告の承認欄
        if (row.actual().stream().anyMatch(Mark::hasDate) && actualCheck != null) {
            if (!approved) {
                notes.add("申請が承認済みにならないので、実績報告の承認日は取り込みません。");
            } else if (!apply && !req.isApproved()) {
                // 読み込みのとき：これから承認済みにする申請は、承認したあとで実績確認が始まるかで決まる
                if (req.getActualConfirmedOn() == null) {
                    notes.add("勤怠の時間外が申請と合っていない（または勤怠がない）ので、実績確認が始まらず、実績報告の承認日は取り込めません。");
                } else {
                    actions.add("実績報告の承認日を取り込む");
                }
            } else {
                importActual(id, row, apply, actions, notes);
            }
        }
        return new RowResult(row.date(), row.getPlannedLabel(), row.getActualLabel(), status, actions, notes);
    }

    private void importActual(int id, Row row, boolean apply, List<String> actions, List<String> notes) {
        OvertimeActualCheckPort.State state = actualCheck.stateOf(id);
        if (state.status() == null) {
            notes.add("実績確認が始まっていません（勤怠の時間外が申請と合っていない、または勤怠がありません）。");
            return;
        }
        // すでに確認した段は、確認日を時間外申請書の日付に直す
        actualCheck.checkedDates(id).forEach((level, current) -> {
            Mark mark = markFor(level, row.actual());
            if (!mark.hasDate() || mark.date().equals(current)) return;
            if (apply) actualCheck.updateCheckedOn(id, level, mark.date());
            actions.add("実績の" + level + "段目（" + columnName(level) + "）の確認日を "
                    + label(current) + " → " + mark.getLabel() + " に直す");
        });
        if (OvertimeActualCheckPort.CONFIRMED.equals(state.status())) {
            return;
        }
        int maxLevel = workflow.getRoutes(RequestType.OVERTIME, List.of(id)).getOrDefault(id, List.of()).size();
        int level = state.level();
        while (level >= 1 && level <= Math.max(maxLevel, 1)) {
            Mark mark = markFor(level, row.actual());
            String col = columnName(level);
            if (!mark.hasDate()) {
                notes.add(mark.skipped()
                        ? "実績報告の" + col + "欄が「－」ですが、システムの実績確認には" + level + "段目があります。"
                        : "実績報告の" + col + "欄（" + level + "段目）の承認日がまだありません。");
                return;
            }
            if (apply) {
                if (!actualCheck.confirmOn(id, level, mark.date(), NOTE)) {
                    notes.add("実績確認の" + level + "段目を記録できませんでした。");
                    return;
                }
            }
            actions.add("実績の" + level + "段目（" + col + "）を " + mark.getLabel() + " に確認");
            if (!apply) {
                level++;
                if (level > maxLevel) return;
                continue;
            }
            OvertimeActualCheckPort.State next = actualCheck.stateOf(id);
            if (!OvertimeActualCheckPort.PENDING.equals(next.status()) || next.level() == null) return;
            level = next.level();
        }
    }

    /** 「2026-11-06 10:00」などの先頭の日付 */
    static LocalDate dateOf(String dateTime) {
        if (dateTime == null || dateTime.length() < 10) return null;
        try {
            return LocalDate.parse(dateTime.substring(0, 10));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String label(LocalDate d) {
        return d == null ? "（なし）" : d.getMonthValue() + "/" + d.getDayOfMonth();
    }

    /** 段 → 承認欄：1段目→課長、2段目→所属長、3段目以降→社長 */
    static Mark markFor(int level, List<Mark> marks) {
        return marks.get(Math.min(level, 3) - 1);
    }

    static String columnName(int level) {
        return OvertimeFormApprovals.COLUMN_NAMES.get(Math.min(level, 3) - 1);
    }
}
