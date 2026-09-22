package com.example.kintai.application.service;

import com.example.application.port.out.WorkTypePort;
import com.example.entity.WorkType;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.kintai.domain.port.out.SaveAttendanceRecordPort;
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
    private static final int STANDARD_WORK_MINUTES = 480; // 8時間

    private final SaveAttendanceRecordPort saveAttendanceRecordPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final PaidLeavePort paidLeavePort;
    private final TransactionManager transactionManager;
    private final WorkTypePort workTypePort;
    private final OvertimeRequestPort overtimeRequestPort;

    public UpdateAttendanceService(SaveAttendanceRecordPort saveAttendanceRecordPort,
                                    LoadAttendanceRecordPort loadAttendanceRecordPort,
                                    PaidLeavePort paidLeavePort,
                                    TransactionManager transactionManager,
                                    WorkTypePort workTypePort,
                                    OvertimeRequestPort overtimeRequestPort) {
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
            Optional<WorkType> paidLeaveWorkType = workTypes.stream()
                    .filter(wt -> "有給".equals(wt.getName()) || wt.isPaid())
                    .findFirst();
            int paidLeaveId = paidLeaveWorkType.map(WorkType::getId).orElse(-1);

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

                        if (toMin < fromMin) {
                            throw new IllegalArgumentException("退勤時刻が出勤時刻より前になっています: " + date);
                        }
                        if (toMin - fromMin > 24 * 60) {
                            throw new IllegalArgumentException("実働時間が24時間を超えています: " + date);
                        }
                    }
                    if (paidLeaveId != -1 && cmd.getAbstractId() != null && cmd.getAbstractId() == paidLeaveId) {
                        if (hasFrom || hasTo) {
                            throw new IllegalArgumentException("有給申請日に勤務実績が入力されています: " + date);
                        }
                    }
                }

                Optional<AttendanceRecord> oldRecord = loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, date);
                int oldStatusId = oldRecord.map(AttendanceRecord::getAbstractId).filter(id -> id != null).orElse(-1);
                int newStatusId = cmd.getAbstractId() != null ? cmd.getAbstractId() : -1;

                if (paidLeaveId != -1) {
                    int staffId = Integer.parseInt(cmd.getEmployeeId());
                    if (oldStatusId != paidLeaveId && newStatusId == paidLeaveId) {
                        paidLeavePort.decrementDays(staffId);
                    } else if (oldStatusId == paidLeaveId && newStatusId != paidLeaveId) {
                        paidLeavePort.incrementDays(staffId);
                    }
                }

                WorkTime workTime = (hasFrom && hasTo)
                        ? new WorkTime(LocalTime.parse(cmd.getFromTime()), LocalTime.parse(cmd.getToTime()))
                        : null;

                AttendanceRecord record = new AttendanceRecord(employeeId, date, workTime);
                record.setWorkWeek(cmd.getWeek());
                record.setAbstractId(cmd.getAbstractId());
                record.setWorkDescription(cmd.getMemo());
                record.setCorrectionId(cmd.getCorrectionId());
                record.setCorrectionUsTime(cmd.getCorrectionUsTime());
                record.setCorrectionMidTime(cmd.getCorrectionMidTime());
                record.setIndirectTime(cmd.getIndirectTime());

                // 給与計上用の合計勤務時間（休憩控除・補正済み）は AttendanceRecord.calculateWorkDuration() を単一の正とする。
                // 一方、時間外（残業・要事前承認）の判定は補正を含まない実働時間で行う。補正は派遣先ごとの
                // 勤務時間計上ルールの差異を吸収するための給与計算上の調整であり、実際に働いた時間の長さ
                // （＝事前承認が必要な「残業」）とは無関係のため、補正込みの時間で時間外を判定すると、
                // 定時どおりに退勤した日でも補正分だけ「残業扱い」になってしまう。
                Integer correctedTotalWorkTime = null;
                Integer correctedTotalDirectWorkTime = null;
                int overtimeMinutes = 0;
                if (workTime != null) {
                    int corrected = (int) record.calculateWorkDuration().toMinutes();
                    correctedTotalWorkTime = corrected;
                    int indirect = cmd.getIndirectTime() != null ? cmd.getIndirectTime() : 0;
                    correctedTotalDirectWorkTime = corrected - indirect;

                    int rawAfterBreak = (int) record.calculateRawWorkDuration().toMinutes();
                    overtimeMinutes = Math.max(0, rawAfterBreak - STANDARD_WORK_MINUTES);
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
                boolean isPaidLeave = paidLeaveId != -1 && newStatusId == paidLeaveId;
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
        });
    }
}
