package com.example.kintai.application.service;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.CompanyHolidayPort;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeFormExportPort;
import com.example.kintai.domain.port.out.OvertimeFormExportPort.FormRow;
import com.example.kintai.domain.port.out.OvertimeFormExportPort.OvertimeForm;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort.FormRequest;
import com.example.kintai.domain.service.JapaneseHolidayCalendar;
import com.example.kintai.domain.service.OvertimeFormCalculator;
import com.example.kintai.domain.service.OvertimeFormCalculator.Step;
import com.example.kintai.domain.service.OvertimeFormCalculator.TimeCells;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社員1人・1か月分の残業事前申請と勤怠の実績を、個人別時間外勤務申請兼実績報告書（Excel）にする。
 * 出すのは申請中・承認済みの申請（取り下げ・却下は出さない）。
 */
public class OvertimeFormExportService {

    private final OvertimeRequestPort overtimeRequestPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final CompanyHolidayPort companyHolidayPort;
    private final OvertimeFormExportPort exportPort;

    /** 残業の実績確認（実績報告の承認欄の日付）。null なら勤怠の確認日などから決める */
    private com.example.kintai.domain.port.out.OvertimeActualCheckPort actualCheckPort;

    public OvertimeFormExportService withActualCheck(com.example.kintai.domain.port.out.OvertimeActualCheckPort port) {
        this.actualCheckPort = port;
        return this;
    }

    public OvertimeFormExportService(OvertimeRequestPort overtimeRequestPort, LoadAttendanceRecordPort loadAttendanceRecordPort,
                                     CompanyHolidayPort companyHolidayPort, OvertimeFormExportPort exportPort) {
        this.overtimeRequestPort = overtimeRequestPort;
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.companyHolidayPort = companyHolidayPort;
        this.exportPort = exportPort;
    }

    /** Excel ファイルの中身 */
    public byte[] export(Subordinate staff, YearMonth month) {
        return exportPort.write(build(staff, month));
    }

    /** 申請書の中身（Excel に書く前の形） */
    public OvertimeForm build(Subordinate staff, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<FormRequest> requests = overtimeRequestPort.findForForm(staff.getStaffId(), from, to);
        if (requests.isEmpty()) {
            throw new IllegalArgumentException(staff.getName() + " さんの " + month.getYear() + "年" + month.getMonthValue()
                    + "月の残業事前申請（申請中・承認済み）がありません。");
        }
        if (requests.size() > OvertimeFormExportPort.MAX_ROWS) {
            throw new IllegalStateException("申請が " + requests.size() + " 件あり、様式（" + OvertimeFormExportPort.MAX_ROWS
                    + "行）に入りきりません。");
        }
        Map<LocalDate, AttendanceRecord> attendance = new HashMap<>();
        if (loadAttendanceRecordPort != null) {
            for (AttendanceRecord r : loadAttendanceRecordPort.loadByEmployeeAndMonth(new EmployeeId(staff.getStaffId()), month)) {
                attendance.put(r.getWorkDate(), r);
            }
        }
        Map<LocalDate, String> companyHolidays = companyHolidayPort == null ? Map.of()
                : companyHolidayPort.findCompanyHolidays(from, to);

        java.util.Map<Integer, List<Step>> actualSteps = actualCheckPort == null ? java.util.Map.of()
                : actualCheckPort.findSteps(requests.stream().map(r -> r.request().getId()).toList());
        List<FormRow> rows = new ArrayList<>();
        String chief = null;
        String head = null;
        for (FormRequest fr : requests) {
            OvertimeRequest req = fr.request();
            List<Step> steps = fr.steps();

            LocalDate date = req.getTargetDate();
            boolean holiday = StaffWorkSummaryCalculator.isWeekend(date) || JapaneseHolidayCalendar.isHoliday(date)
                    || companyHolidays.containsKey(date);
            TimeCells planned = OvertimeFormCalculator.planned(minutes(req.getPlannedStartTime()),
                    plannedEnd(req.getPlannedStartTime(), req.getPlannedEndTime()), req.getPlannedOvertimeMinutes(),
                    req.getCorrectionMinutes(), req.getCorrectionMidMinutes(), holiday);
            AttendanceRecord record = attendance.get(date);
            TimeCells actual = actual(record, holiday);
            LocalDate autoApproved = req.isApproved() && req.getDecidedAt() != null
                    ? LocalDate.parse(req.getDecidedAt().substring(0, 10)) : null;
            // 実績報告の承認欄：勤怠がまだなければ空欄。実績が申請と合っていて承認済みなら、段ごとの承認日も使える
            List<com.example.kintai.domain.service.OvertimeFormCalculator.ApprovalMark> actualApprovals;
            if (actualCheckPort != null) {
                // 実績確認：段ごとの確認日（まだ確認していない段は空欄、ルートにない段は「－」）
                List<Step> checks = actualSteps.getOrDefault(req.getId(), List.of());
                actualApprovals = checks.isEmpty()
                        ? OvertimeFormCalculator.actualApprovals(steps, req.getBaseLevels(), null, false, autoApproved)
                        : OvertimeFormCalculator.approvals(checks, null);
            } else if (record == null || record.getWorkTime() == null) {
                actualApprovals = OvertimeFormCalculator.actualApprovals(steps, req.getBaseLevels(), null, false, autoApproved);
            } else {
                boolean matches = req.isApproved() && OvertimeFormCalculator.actualConfirmedOn(req.getPlannedOvertimeMinutes(),
                        record.getOvertimeMinutes(), holiday, null, date) != null;
                actualApprovals = OvertimeFormCalculator.actualApprovals(steps, req.getBaseLevels(),
                        req.getActualConfirmedOn(), matches, autoApproved);
            }
            rows.add(new FormRow(date, req.getProjectName(), planned,
                    req.getCategory() != null ? req.getCategory() : OvertimeRequest.DEFAULT_CATEGORY,
                    OvertimeFormCalculator.approvals(steps, autoApproved), actual, req.getReason(), actualApprovals));
            // 課長名・所属長名は、いちばん新しい申請のルートの1段目・2段目の承認者
            String c = OvertimeFormCalculator.approverName(steps, 1);
            String h = OvertimeFormCalculator.approverName(steps, 2);
            if (c != null) chief = c;
            if (h != null) head = h;
        }
        return new OvertimeForm(month, staff.getDepartmentName(), staff.getName(), chief, head, rows);
    }

    private static TimeCells actual(AttendanceRecord record, boolean holiday) {
        if (record == null || record.getWorkTime() == null) {
            return TimeCells.EMPTY;
        }
        WorkTime wt = record.getWorkTime();
        return OvertimeFormCalculator.actual(wt.getStartMinuteOfDay(), wt.getEndMinuteFromStartDay(), holiday,
                record.getOvertimeMinutes(), record.getCorrectionMidTime() == null ? 0 : record.getCorrectionMidTime());
    }

    private static Integer minutes(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        return LocalTime.parse(hhmm).toSecondOfDay() / 60;
    }

    /** 予定退勤（分）。予定出勤より前の時刻なら翌日として 24時間を足す */
    private static Integer plannedEnd(String start, String end) {
        Integer e = minutes(end);
        if (e == null) return null;
        return OvertimeRequest.isPlannedEndNextDay(start, end) ? e + 24 * 60 : e;
    }
}
