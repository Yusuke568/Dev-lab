package com.example.kintai.application.service;

import com.example.application.port.out.WorkTypePort;
import com.example.entity.WorkType;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.kintai.domain.port.out.SaveAttendanceRecordPort;
import com.example.kintai.domain.service.WeeklyOvertimeCalculator;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.shared.transaction.TransactionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠記録を更新するユースケースの実装。
 *
 * 旧 com.example.application.service.UpdateKintaiService を、
 * ヘキサゴナルアーキテクチャの kintai ドメイン（SaveAttendanceRecordPort経由）に
 * 統合したもの。バリデーション・有給消化連動のロジックは踏襲している。
 */
public class UpdateAttendanceService implements UpdateAttendanceUseCase {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PENDING_APPROVAL = 1;
    private static final int STATUS_APPROVED = 2;

    private final SaveAttendanceRecordPort saveAttendanceRecordPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final PaidLeavePort paidLeavePort;
    private final TransactionManager transactionManager;
    private final WorkTypePort workTypePort;
    private final OvertimeRequestPort overtimeRequestPort;
    /** 残業の実績確認。null なら扱わない */
    private OvertimeActualCheckService actualCheck;

    public UpdateAttendanceService withActualCheck(OvertimeActualCheckService service) {
        this.actualCheck = service;
        return this;
    }

    /** 週40時間の時間外（休日出勤の日の分）を計算し直す。null なら日ごとの計算だけ */
    private final WeeklyOvertimeService weeklyOvertimeService;

    public UpdateAttendanceService(SaveAttendanceRecordPort saveAttendanceRecordPort,
                                    LoadAttendanceRecordPort loadAttendanceRecordPort,
                                    PaidLeavePort paidLeavePort,
                                    TransactionManager transactionManager,
                                    WorkTypePort workTypePort,
                                    OvertimeRequestPort overtimeRequestPort) {
        this(saveAttendanceRecordPort, loadAttendanceRecordPort, paidLeavePort, transactionManager, workTypePort,
                overtimeRequestPort, null);
    }

    public UpdateAttendanceService(SaveAttendanceRecordPort saveAttendanceRecordPort,
                                    LoadAttendanceRecordPort loadAttendanceRecordPort,
                                    PaidLeavePort paidLeavePort,
                                    TransactionManager transactionManager,
                                    WorkTypePort workTypePort,
                                    OvertimeRequestPort overtimeRequestPort,
                                    WeeklyOvertimeService weeklyOvertimeService) {
        this.weeklyOvertimeService = weeklyOvertimeService;
        this.saveAttendanceRecordPort = saveAttendanceRecordPort;
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.paidLeavePort = paidLeavePort;
        this.transactionManager = transactionManager;
        this.workTypePort = workTypePort;
        this.overtimeRequestPort = overtimeRequestPort;
    }

    @Override
    public void updateAttendance(List<AttendanceUpdateCommand> commands) {
        if (commands == null || commands.isEmpty()) return;

        transactionManager.executeInTransaction(() -> {
            List<WorkType> workTypes = workTypePort.findAll();
            // 勤務区分ごとの有給消費日数（有給 1 日、半休 0.5 日、それ以外 0 日。適用マスタで設定）
            java.util.Map<Integer, Double> paidLeaveUse = new java.util.HashMap<>();
            for (WorkType wt : workTypes) {
                paidLeaveUse.put(wt.getId(), wt.getPaidLeaveDays());
            }

            for (AttendanceUpdateCommand cmd : commands) {
                EmployeeId employeeId = new EmployeeId(cmd.getEmployeeId());
                LocalDate date = cmd.getDate();
                boolean hasFrom = cmd.getFromTime() != null && cmd.getFromTime().contains(":");
                boolean hasTo = cmd.getToTime() != null && cmd.getToTime().contains(":");

                if (!cmd.isTemporary()) {
                    // Backend Validation (UC-VAL-01, 02, 03)
                    if (hasFrom && hasTo) {
                        String[] fromParts = cmd.getFromTime().split(":");
                        String[] toParts = cmd.getToTime().split(":");
                        int fromMin = Integer.parseInt(fromParts[0]) * 60 + Integer.parseInt(fromParts[1]);
                        int toMin = Integer.parseInt(toParts[0]) * 60 + Integer.parseInt(toParts[1]);

                        // 退勤が出勤より前の時刻なら、日をまたいで翌日に退勤したものとして扱う
                        if (toMin < fromMin) {
                            toMin += 24 * 60;
                        }
                        if (toMin - fromMin > 24 * 60) {
                            throw new IllegalArgumentException("実働時間が24時間を超えています: " + date);
                        }
                    }
                    // 1日まるごとの有給の日に出退勤があるのは入力ミス（半休は出退勤があってよい）
                    if (cmd.getAbstractId() != null && paidLeaveUse.getOrDefault(cmd.getAbstractId(), 0.0) >= 1) {
                        if (hasFrom || hasTo) {
                            throw new IllegalArgumentException("有給申請日に勤務実績が入力されています: " + date);
                        }
                    }
                }

                Optional<AttendanceRecord> oldRecord = loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, date);
                int oldStatusId = oldRecord.map(AttendanceRecord::getAbstractId).filter(id -> id != null).orElse(-1);
                int newStatusId = cmd.getAbstractId() != null ? cmd.getAbstractId() : -1;

                // 有給残日数：前の勤務区分で使っていた日数を戻し、新しい勤務区分で使う日数を引く
                // （例：出勤 → 半休 で 0.5 日減る、有給 → 半休 で 0.5 日戻る）
                double oldUse = paidLeaveUse.getOrDefault(oldStatusId, 0.0);
                double newUse = paidLeaveUse.getOrDefault(newStatusId, 0.0);
                if (oldUse != newUse) {
                    paidLeavePort.adjustDays(Integer.parseInt(cmd.getEmployeeId()), oldUse - newUse);
                }

                WorkTime workTime = (hasFrom && hasTo)
                        ? new WorkTime(LocalTime.parse(cmd.getFromTime()), LocalTime.parse(cmd.getToTime()))
                        : null;

                AttendanceRecord record = new AttendanceRecord(employeeId, date, workTime);
                if (workTime == null) {
                    // 出勤ボタンだけ押した日などは、入力された方の時刻だけを残す
                    record.setPartialTimes(hasFrom ? LocalTime.parse(cmd.getFromTime()) : null,
                            hasTo ? LocalTime.parse(cmd.getToTime()) : null);
                }
                record.setWorkWeek(cmd.getWeek());
                record.setAbstractId(cmd.getAbstractId());
                record.setWorkDescription(cmd.getMemo());
                record.setCorrectionId(cmd.getCorrectionId());
                record.setCorrectionUsTime(cmd.getCorrectionUsTime());
                record.setCorrectionMidTime(cmd.getCorrectionMidTime());
                record.setIndirectTime(cmd.getIndirectTime());

                // 総勤務時間・勤務時間(直接)・時間外は、作業報告書と同じ計算（AttendanceRecord）を単一の正とする。
                //   総勤務時間 ＝ 休憩控除後の実働 ＋ 補正(通) ＋ 補正(深) ＋ 勤務時間(間接)
                //   勤務時間(直接) ＝ 総勤務時間 − 勤務時間(間接)
                //   時間外 ＝ 総勤務時間 − 8:00（補正・間接も含めて数える）
                Integer correctedTotalWorkTime = null;
                Integer correctedTotalDirectWorkTime = null;
                int overtimeMinutes = 0;
                if (workTime != null) {
                    correctedTotalWorkTime = (int) record.calculateWorkDuration().toMinutes();
                    correctedTotalDirectWorkTime = (int) record.calculateDirectWorkDuration().toMinutes();
                    overtimeMinutes = record.calculateOvertimeMinutes();
                }
                record.setOvertimeMinutes(overtimeMinutes);
                record.setTotalWorkTime(correctedTotalWorkTime);
                record.setTotalDirectWorkTime(correctedTotalDirectWorkTime);

                // 時間外は「事前申請」が主経路。承認済みの事前申請がある日は追加承認をスキップし、
                // 事前申請のない（想定外の）時間外のみ従来どおり承認待ちにする安全策として残す。
                boolean isOvertime = overtimeMinutes > 0;
                boolean hasApprovedOvertimeRequest = isOvertime
                        && overtimeRequestPort.findByEmployeeAndDate(employeeId, date)
                                .map(r -> r.isApproved())
                                .orElse(false);
                boolean isPaidLeave = newUse > 0; // 有給・半休は承認待ちにする
                boolean needsApproval = isPaidLeave || (isOvertime && !hasApprovedOvertimeRequest);

                if (cmd.isTemporary()) {
                    record.setApprovalStatus(STATUS_DRAFT);
                } else if (needsApproval) {
                    record.setApprovalStatus(STATUS_PENDING_APPROVAL);
                } else {
                    record.setApprovalStatus(STATUS_APPROVED);
                }

                saveAttendanceRecordPort.save(record);
            }

            // 週40時間：保存した日を含む週（日曜〜土曜）の、休日出勤の日の時間外を計算し直す
            java.util.Set<String> touched = new java.util.LinkedHashSet<>();
            for (AttendanceUpdateCommand cmd : commands) {
                touched.add(cmd.getEmployeeId() + "/" + cmd.getDate());
            }
            if (weeklyOvertimeService != null) {
                java.util.Set<String> weeks = new java.util.LinkedHashSet<>();
                for (AttendanceUpdateCommand cmd : commands) {
                    weeks.add(cmd.getEmployeeId() + "/" + WeeklyOvertimeCalculator.weekStart(cmd.getDate()));
                }
                for (String key : weeks) {
                    String[] parts = key.split("/");
                    LocalDate weekStart = LocalDate.parse(parts[1]);
                    recalculateHolidayOvertime(new EmployeeId(parts[0]), weekStart, paidLeaveUse);
                    // 週の休日出勤の日は時間外が変わることがあるので、実績の確認もし直す
                    for (int i = 0; i < 7; i++) {
                        LocalDate d = weekStart.plusDays(i);
                        if (weeklyOvertimeService.isHoliday(d, weeklyOvertimeService.companyHolidays(d, d))) {
                            touched.add(parts[0] + "/" + d);
                        }
                    }
                }
            }
            // 時間外申請書の実績の承認欄：残業事前申請と実績を比べて、実績の確認日を入れる・消す
            for (String key : touched) {
                String[] parts = key.split("/");
                confirmActual(new EmployeeId(parts[0]), LocalDate.parse(parts[1]));
            }
        });
    }

    /**
     * その日の残業事前申請（申請中・承認済み）と、勤怠の時間外の実績を比べる。
     *   合っている（休日は予定以下）・時間外がない → 実績の確認日を今日にする（すでに入っていればそのまま）
     *   合わない → 消す（申請し直して承認された日を使う）
     */
    private void confirmActual(EmployeeId employeeId, LocalDate date) {
        if (overtimeRequestPort == null) return;
        Optional<OvertimeRequest> found = overtimeRequestPort.findByEmployeeAndDate(employeeId, date);
        if (found.isEmpty() || !(found.get().isPending() || found.get().isApproved())) return;
        Optional<AttendanceRecord> record = loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, date);
        if (record.isEmpty() || record.get().getWorkTime() == null) return; // まだ実績がない
        OvertimeRequest req = found.get();
        boolean holiday = weeklyOvertimeService != null
                ? weeklyOvertimeService.isHoliday(date, weeklyOvertimeService.companyHolidays(date, date))
                : date.getDayOfWeek().getValue() >= 6;
        LocalDate confirmed = com.example.kintai.domain.service.OvertimeFormCalculator.actualConfirmedOn(
                req.getPlannedOvertimeMinutes(), record.get().getOvertimeMinutes(), holiday,
                req.getActualConfirmedOn(), LocalDate.now());
        if (!java.util.Objects.equals(confirmed, req.getActualConfirmedOn())) {
            overtimeRequestPort.updateActualConfirmedOn(req.getId(), confirmed);
        }
        // 実績が合えば実績確認を始め、合わなくなればやめる
        if (actualCheck != null) actualCheck.sync(req.getId());
    }

    /**
     * 週の休日出勤の日の時間外を、週40時間の決まりで計算し直して保存する。
     * 提出済み（一時保存でない）の日は、時間外が変われば承認の要否も決め直す。
     */
    private void recalculateHolidayOvertime(EmployeeId employeeId, LocalDate weekStart, java.util.Map<Integer, Double> paidLeaveUse) {
        java.util.Map<Integer, Integer> credits = new java.util.HashMap<>();
        paidLeaveUse.forEach((id, days) -> credits.put(id, (int) Math.round(days * WeeklyOvertimeCalculator.DAILY_MINUTES)));
        java.util.Map<LocalDate, Integer> overtime = WeeklyOvertimeCalculator.holidayOvertime(
                weeklyOvertimeService.loadWeek(employeeId, weekStart, credits));
        overtime.forEach((date, minutes) -> {
            Optional<AttendanceRecord> found = loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, date);
            if (found.isEmpty() || found.get().getOvertimeMinutes() == minutes) {
                return;
            }
            AttendanceRecord record = found.get();
            record.setOvertimeMinutes(minutes);
            if (record.getApprovalStatus() != STATUS_DRAFT) {
                boolean isPaidLeave = record.getAbstractId() != null && paidLeaveUse.getOrDefault(record.getAbstractId(), 0.0) > 0;
                boolean hasApprovedOvertimeRequest = minutes > 0
                        && overtimeRequestPort.findByEmployeeAndDate(employeeId, date).map(r -> r.isApproved()).orElse(false);
                boolean needsApproval = isPaidLeave || (minutes > 0 && !hasApprovedOvertimeRequest);
                record.setApprovalStatus(needsApproval ? STATUS_PENDING_APPROVAL : STATUS_APPROVED);
            }
            saveAttendanceRecordPort.save(record);
        });
    }
}
