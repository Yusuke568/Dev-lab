package com.example.kintai.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.approval.domain.port.out.OrganizationPort;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.master.domain.model.Department;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 残業事前申請の受け付け（日またぎの退勤、補正・間接だけで時間外になった日） */
class OvertimeRequestSubmitTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 5);

    /** insert された予定時間外（分） */
    private final List<Integer> inserted = new ArrayList<>();
    private AttendanceRecord recorded;
    /** decide された申請（"ID:承認:判定者"） */
    private final List<String> decided = new ArrayList<>();
    /** 開発1課の承認者（300 なら 300 本人の申請は自動承認） */
    private Integer approverOfSection = 300;

    private final OvertimeRequestPort requestPort = new OvertimeRequestPort() {
        @Override public Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId employeeId, LocalDate date) { return Optional.empty(); }
        @Override public List<OvertimeRequest> findByEmployee(EmployeeId employeeId) { return List.of(); }
        @Override public Optional<OvertimeRequest> findById(int requestId) { return Optional.empty(); }
        @Override public List<OvertimeRequest> findPendingForApprover(String approverId) { return List.of(); }
        @Override public int insert(EmployeeId employeeId, LocalDate date, int plannedOvertimeMinutes, String start, String end, String reason) {
            inserted.add(plannedOvertimeMinutes);
            return inserted.size();
        }
        @Override public void resubmit(int requestId, int plannedOvertimeMinutes, String start, String end, String reason) { }
        @Override public void decide(int requestId, boolean approve, String decidedBy) { decided.add(requestId + ":" + approve + ":" + decidedBy); }
        @Override public void advanceLevel(int requestId) { }
    };

    /** 126 は開発1課（承認者 300） */
    private final OrganizationPort organization = new OrganizationPort() {
        @Override public Map<Integer, Department> findAllDepartments() { return Map.of(2, new Department(2, "開発1課", null, approverOfSection, 0)); }
        @Override public Integer findDepartmentIdOfStaff(int staffId) { return 2; }
        @Override public int findMaxLevels(RequestType requestType) { return 1; }
    };

    private final ApprovalStepPort steps = new ApprovalStepPort() {
        @Override public void replaceSteps(RequestType requestType, int requestId, List<Integer> approverStaffIds) { }
        @Override public List<ApprovalStep> findSteps(RequestType requestType, int requestId) { return List.of(); }
        @Override public Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> requestIds) { return Map.of(); }
        @Override public void recordDecision(RequestType requestType, int requestId, int level, String decision) { }
    };

    private final LoadAttendanceRecordPort attendance = new LoadAttendanceRecordPort() {
        @Override public List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId employeeId, YearMonth yearMonth) { return List.of(); }
        @Override public Optional<AttendanceRecord> loadByEmployeeAndDate(EmployeeId employeeId, LocalDate date) { return Optional.ofNullable(recorded); }
        @Override public long sumOvertimeMinutesByEmployeeAndYear(EmployeeId employeeId, int year) { return 0; }
    };

    private final OvertimeRequestService service =
            new OvertimeRequestService(requestPort, new ApprovalWorkflowService(steps, organization), attendance);

    @Test
    void 翌日の退勤も残業として申請できる() {
        // 13:00〜翌1:30 → 18:00〜24:00 の6時間 ＋ 1:30 ＝ 7:30
        service.submit("126", DAY, "13:00", "01:30", "リリース対応");

        assertEquals(List.of(7 * 60 + 30), inserted);
        assertEquals(7 * 60 + 30, OvertimeRequest.calculatePlannedOvertimeMinutes("13:00", "01:30"));
        assertTrue(OvertimeRequest.isPlannedEndNextDay("13:00", "01:30"));
    }

    @Test
    void 同じ日のうちの退勤はこれまでどおり() {
        assertEquals(2 * 60 + 30, OvertimeRequest.calculatePlannedOvertimeMinutes("09:00", "20:30"));
        assertEquals(30 + 60, OvertimeRequest.calculatePlannedOvertimeMinutes("08:30", "19:00"));
        assertEquals(0, OvertimeRequest.calculatePlannedOvertimeMinutes("09:00", "17:45"));
    }

    @Test
    void 補正や間接だけで時間外になった日は勤怠の時間外で申請できる() {
        // 9:00〜17:45、補正0:15、間接2:00 → 総勤務10:00、時間外2:00（予定時刻だけでは時間外0）
        recorded = new AttendanceRecord(new EmployeeId("126"), DAY, new WorkTime(LocalTime.of(9, 0), LocalTime.of(17, 45)));
        recorded.setOvertimeMinutes(120);

        service.submit("126", DAY, "09:00", "17:45", "週報纏め");

        assertEquals(List.of(120), inserted);
    }

    @Test
    void 上位に承認者がいない承認者本人の申請は自動で承認済みになる() {
        boolean auto = service.submit("300", DAY, "09:00", "20:00", "障害対応");

        assertTrue(auto);
        assertEquals(List.of("1:true:300"), decided);
    }

    @Test
    void 承認者がいる人の申請は承認待ちのまま() {
        assertFalse(service.submit("126", DAY, "09:00", "20:00", "障害対応"));
        assertTrue(decided.isEmpty());
    }

    @Test
    void 時間外がない日は申請できない() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.submit("126", DAY, "09:00", "17:45", ""));
        assertTrue(e.getMessage().startsWith("時間外が発生していません"));
        assertTrue(inserted.isEmpty());
    }
}
