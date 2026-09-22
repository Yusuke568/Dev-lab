package com.example.kintai.application.service;

import com.example.kintai.application.port.in.ApproveAttendanceUseCase;
import com.example.kintai.application.port.in.GetApprovalHistoryUseCase;
import com.example.kintai.application.port.in.GetPendingApprovalsUseCase;
import com.example.kintai.domain.model.attendance.ApprovalHistoryEntry;
import com.example.kintai.domain.model.attendance.ApprovalHistoryFilter;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.PendingApprovalItem;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadApprovalHistoryPort;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.LoadPendingApprovalsPort;
import com.example.kintai.domain.port.out.SaveApprovalHistoryPort;
import com.example.kintai.domain.port.out.SaveAttendanceRecordPort;

import java.time.LocalDate;
import java.util.List;

/**
 * 勤怠の承認ワークフローのユースケース実装。
 *
 * 承認ステータス: 0=下書き/一時保存, 1=申請中, 2=承認済み, 3=却下
 * 承認・却下の決定は kintai_approval_history に履歴として記録される。
 */
public class AttendanceApprovalService implements GetPendingApprovalsUseCase, ApproveAttendanceUseCase, GetApprovalHistoryUseCase {

    private static final int STATUS_SUBMITTED = 1;
    private static final int STATUS_APPROVED = 2;
    private static final int STATUS_REJECTED = 3;

    private final LoadPendingApprovalsPort loadPendingApprovalsPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final SaveAttendanceRecordPort saveAttendanceRecordPort;
    private final SaveApprovalHistoryPort saveApprovalHistoryPort;
    private final LoadApprovalHistoryPort loadApprovalHistoryPort;

    public AttendanceApprovalService(LoadPendingApprovalsPort loadPendingApprovalsPort,
                                      LoadAttendanceRecordPort loadAttendanceRecordPort,
                                      SaveAttendanceRecordPort saveAttendanceRecordPort,
                                      SaveApprovalHistoryPort saveApprovalHistoryPort,
                                      LoadApprovalHistoryPort loadApprovalHistoryPort) {
        this.loadPendingApprovalsPort = loadPendingApprovalsPort;
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.saveAttendanceRecordPort = saveAttendanceRecordPort;
        this.saveApprovalHistoryPort = saveApprovalHistoryPort;
        this.loadApprovalHistoryPort = loadApprovalHistoryPort;
    }

    @Override
    public List<PendingApprovalItem> getPendingApprovals() {
        return loadPendingApprovalsPort.loadAll();
    }

    @Override
    public void decide(String employeeId, LocalDate date, boolean approve, String decidedBy) {
        EmployeeId id = new EmployeeId(employeeId);
        AttendanceRecord record = loadAttendanceRecordPort.loadByEmployeeAndDate(id, date)
                .orElseThrow(() -> new IllegalArgumentException("対象の勤怠記録が見つかりません: " + employeeId + " " + date));

        if (record.getApprovalStatus() != STATUS_SUBMITTED) {
            throw new IllegalStateException("申請中の記録のみ承認・却下できます: " + employeeId + " " + date);
        }

        record.setApprovalStatus(approve ? STATUS_APPROVED : STATUS_REJECTED);
        saveAttendanceRecordPort.save(record);
        saveApprovalHistoryPort.record(employeeId, date, approve, decidedBy);
    }

    @Override
    public List<ApprovalHistoryEntry> getHistory(ApprovalHistoryFilter filter) {
        return loadApprovalHistoryPort.loadAll(filter);
    }
}
