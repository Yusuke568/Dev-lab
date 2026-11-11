package com.example.kintai.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 残業事前申請を承認するときに見る、申請者のその月の時間外の状況。
 *
 *   実績        … 申請日以外の日の勤怠に保存されている時間外
 *   承認済の予定 … 承認済みの残業事前申請のうち、まだ時間外の実績がない日の予定
 *   ほかの申請中 … 承認待ちのほかの申請のうち、まだ時間外の実績がない日の予定
 *   この申請    … 今回の申請の予定時間外
 * 基準線は、申請者の承認ルートの「当月の時間外が○時間以上」の段と、労働基準法の原則の上限（月45時間）。
 */
public class OvertimeStatusService {

    /** 労働基準法（36協定）の原則の上限：月45時間 */
    public static final int LAW_MONTHLY_LIMIT_MINUTES = 45 * 60;

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final OvertimeRequestPort overtimeRequestPort;
    private final ApprovalWorkflowService approvalWorkflowService;

    public OvertimeStatusService(LoadAttendanceRecordPort loadAttendanceRecordPort, OvertimeRequestPort overtimeRequestPort,
                                 ApprovalWorkflowService approvalWorkflowService) {
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.overtimeRequestPort = overtimeRequestPort;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    /** 基準線1本（分・名前・労働基準法の線か） */
    public record Threshold(int minutes, String label, boolean law) {
    }

    /** 予定1件（日・分・種類 APPROVED / PENDING / THIS） */
    public record Planned(int day, int minutes, String kind) {
    }

    /**
     * 申請1件分の状況。
     * actualByDay は日ごとの時間外の実績（添字0が1日。申請日は0）、planned は日ごとの予定（折れ線グラフ用）。
     */
    public record Status(String month, int actualMinutes, int approvedPlannedMinutes, int otherPendingMinutes,
                         int thisRequestMinutes, List<Threshold> thresholds, int[] actualByDay, List<Planned> planned) {
        public int totalMinutes() {
            return actualMinutes + approvedPlannedMinutes + otherPendingMinutes + thisRequestMinutes;
        }
    }

    public Status statusOf(OvertimeRequest request) {
        EmployeeId employeeId = new EmployeeId(request.getEmployeeId());
        LocalDate target = request.getTargetDate();
        YearMonth month = YearMonth.from(target);

        Map<LocalDate, Integer> recorded = new HashMap<>();
        for (AttendanceRecord r : loadAttendanceRecordPort.loadByEmployeeAndMonth(employeeId, month)) {
            recorded.put(r.getWorkDate(), r.getOvertimeMinutes());
        }
        List<OvertimeRequest> others = new ArrayList<>();
        for (OvertimeRequest r : overtimeRequestPort.findByEmployee(employeeId)) {
            if (r.getId() != request.getId() && YearMonth.from(r.getTargetDate()).equals(month)) {
                others.add(r);
            }
        }
        return calculate(request, recorded, others,
                approvalWorkflowService.monthlyOvertimeThresholds(Integer.parseInt(request.getEmployeeId()), null));
    }

    /** 集計そのもの（テストしやすいように分けた） */
    static Status calculate(OvertimeRequest request, Map<LocalDate, Integer> recordedOvertime, List<OvertimeRequest> others,
                            Map<Integer, String> routeThresholds) {
        LocalDate target = request.getTargetDate();
        YearMonth month = YearMonth.from(target);
        int[] actualByDay = new int[month.lengthOfMonth()];
        int actual = 0;
        for (Map.Entry<LocalDate, Integer> e : recordedOvertime.entrySet()) {
            if (!e.getKey().equals(target) && e.getValue() != null && YearMonth.from(e.getKey()).equals(month)) {
                actual += e.getValue();
                actualByDay[e.getKey().getDayOfMonth() - 1] += e.getValue();
            }
        }
        List<Planned> planned = new ArrayList<>();
        int approved = 0;
        int pending = 0;
        for (OvertimeRequest r : others) {
            if (r.getTargetDate().equals(target) || recordedOvertime.getOrDefault(r.getTargetDate(), 0) > 0) {
                continue; // 実績がある日は実績で数える
            }
            if (r.isApproved()) {
                approved += r.getPlannedOvertimeMinutes();
                planned.add(new Planned(r.getTargetDate().getDayOfMonth(), r.getPlannedOvertimeMinutes(), "APPROVED"));
            } else if (r.isPending()) {
                pending += r.getPlannedOvertimeMinutes();
                planned.add(new Planned(r.getTargetDate().getDayOfMonth(), r.getPlannedOvertimeMinutes(), "PENDING"));
            }
        }
        planned.add(new Planned(target.getDayOfMonth(), request.getPlannedOvertimeMinutes(), "THIS"));
        planned.sort((a, b) -> Integer.compare(a.day(), b.day()));
        List<Threshold> thresholds = new ArrayList<>();
        routeThresholds.forEach((minutes, label) -> {
            if (minutes != LAW_MONTHLY_LIMIT_MINUTES) {
                thresholds.add(new Threshold(minutes, label, false));
            }
        });
        thresholds.add(new Threshold(LAW_MONTHLY_LIMIT_MINUTES, "労働基準法の原則の上限（月45時間）", true));
        thresholds.sort((a, b) -> Integer.compare(a.minutes(), b.minutes()));
        return new Status(month(target), actual, approved, pending, request.getPlannedOvertimeMinutes(), thresholds,
                actualByDay, planned);
    }

    private static String month(LocalDate date) {
        return date.getYear() + "年" + date.getMonthValue() + "月";
    }
}
