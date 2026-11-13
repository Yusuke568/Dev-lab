package com.example.kintai.application.service;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.RequestFacts;
import com.example.approval.domain.model.RequestType;
import com.example.kintai.application.port.in.DecideOvertimeRequestUseCase;
import com.example.kintai.application.port.in.GetMyOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.SubmitOvertimeRequestUseCase;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * 残業事前申請のユースケース実装。
 *
 * 申請中・承認済みの申請がすでにある日付への申請は拒否する。
 * 却下された申請に対する再申請は、同じレコードを更新して申請中に戻す。
 * 承認は組織図に基づく承認ルート（ApprovalWorkflowService）に沿って1段ずつ進む。
 */
public class OvertimeRequestService implements SubmitOvertimeRequestUseCase, GetMyOvertimeRequestsUseCase,
        GetPendingOvertimeRequestsUseCase, DecideOvertimeRequestUseCase {

    private final OvertimeRequestPort overtimeRequestPort;
    private final ApprovalWorkflowService approvalWorkflowService;
    /** 勤怠に入力済みの時間外（補正・間接の分を含む）を見るため。null なら見ない */
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    /** 会社休日（休日出勤の申請かを決める）。null なら土日・祝日だけで決める */
    private com.example.kintai.domain.port.out.CompanyHolidayPort companyHolidayPort;

    public OvertimeRequestService withCompanyHolidays(com.example.kintai.domain.port.out.CompanyHolidayPort port) {
        this.companyHolidayPort = port;
        return this;
    }

    public OvertimeRequestService(OvertimeRequestPort overtimeRequestPort, ApprovalWorkflowService approvalWorkflowService) {
        this(overtimeRequestPort, approvalWorkflowService, null);
    }

    public OvertimeRequestService(OvertimeRequestPort overtimeRequestPort, ApprovalWorkflowService approvalWorkflowService,
                                  LoadAttendanceRecordPort loadAttendanceRecordPort) {
        this.overtimeRequestPort = overtimeRequestPort;
        this.approvalWorkflowService = approvalWorkflowService;
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
    }

    @Override
    public boolean submit(String employeeId, LocalDate date, String plannedStartTime, String plannedEndTime, String reason,
                          Integer departmentId, String projectName, String category,
                          Integer correctionMinutes, Integer correctionMidMinutes) {
        EmployeeId id = new EmployeeId(employeeId);
        // 補正（通常・深夜）。入力がなければ、その日の勤怠に入っている補正を使う（勤怠の実績からの申請）
        Optional<AttendanceRecord> record = loadAttendanceRecordPort == null ? Optional.empty()
                : loadAttendanceRecordPort.loadByEmployeeAndDate(id, date);
        int correction = correctionMinutes != null ? correctionMinutes
                : record.map(AttendanceRecord::getCorrectionUsTime).orElse(0);
        int correctionMid = correctionMidMinutes != null ? correctionMidMinutes
                : record.map(AttendanceRecord::getCorrectionMidTime).orElse(0);
        if (correction < 0 || correctionMid < 0 || correction + correctionMid > 24 * 60) {
            throw new IllegalArgumentException("補正の時間が正しくありません。");
        }
        // 予定の出退勤から出した時間に、補正を足す。
        // 休日（土日・祝日・会社休日）は、予定の勤務時間（休憩を除く）を全部時間外の見込みにする
        boolean holiday = isHoliday(date);
        if (holiday && (plannedStartTime == null || plannedStartTime.isBlank()
                || plannedEndTime == null || plannedEndTime.isBlank()) && record.isEmpty()) {
            throw new IllegalArgumentException("休日出勤の申請は、予定出勤時刻と予定退勤時刻を入れてください。");
        }
        int plannedOvertimeMinutes = (holiday
                ? OvertimeRequest.calculateHolidayWorkMinutes(plannedStartTime, plannedEndTime)
                : OvertimeRequest.calculatePlannedOvertimeMinutes(plannedStartTime, plannedEndTime))
                + correction + correctionMid;
        // 時間外には間接の時間も含めるので、出退勤が 9:00〜18:00 の中でも時間外になる日がある。
        // その日の勤怠に時間外が入っていれば、その時間で申請できるようにする
        int recordedOvertimeMinutes = record.map(AttendanceRecord::getOvertimeMinutes).orElse(0);
        plannedOvertimeMinutes = Math.max(plannedOvertimeMinutes, recordedOvertimeMinutes);
        if (plannedOvertimeMinutes <= 0) {
            throw new IllegalArgumentException("時間外が発生していません。標準勤務時間（09:00〜18:00）より早い出勤時刻、または遅い退勤時刻を入力してください。");
        }
        // 申請理由は必須（承認者が判断できるように）
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("申請理由を入れてください。時間外が必要な理由（作業内容など）を書いてください。");
        }
        if (reason.strip().length() > 255) {
            throw new IllegalArgumentException("申請理由は255文字以内で入れてください。");
        }
        reason = reason.strip();
        // PJ名・摘要（時間外申請書に出す）。申請画面からは両方入ってくる。null は古い呼び出し（入れない）
        if (projectName != null) {
            projectName = projectName.strip();
            if (projectName.isEmpty()) {
                throw new IllegalArgumentException("PJ名を入れてください。");
            }
            if (projectName.length() > 100) {
                throw new IllegalArgumentException("PJ名は100文字以内で入れてください。");
            }
        }
        if (category != null && !OvertimeRequest.CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("摘要を選んでください（①自責〜⑤その他）。");
        }
        // 同じ日の申請があれば、状態（申請中・承認済み・却下・取り下げ）に関係なく申請し直す。
        // 内容を書き換えて承認待ちに戻し、承認ルートの最初からもう一度承認を受ける（間違えた時間で承認された申請の訂正など）
        Optional<OvertimeRequest> existing = overtimeRequestPort.findByEmployeeAndDate(id, date);
        // 承認ルートの条件つきの段（当月の時間外が○時間以上・深夜）を決めるための申請の中身
        RequestFacts facts = new RequestFacts(
                monthlyOvertimeMinutesExcept(id, date) + plannedOvertimeMinutes,
                isNight(plannedStartTime, plannedEndTime) || correctionMid > 0);
        // 承認者がいないまま申請だけ残らないよう、保存前にルートが組めるか確かめる
        approvalWorkflowService.checkRouteAvailable(RequestType.OVERTIME, employeeId, departmentId, facts);

        int requestId;
        if (existing.isPresent()) {
            requestId = existing.get().getId();
            overtimeRequestPort.resubmit(requestId, plannedOvertimeMinutes, plannedStartTime, plannedEndTime, reason);
        } else {
            requestId = overtimeRequestPort.insert(id, date, plannedOvertimeMinutes, plannedStartTime, plannedEndTime, reason);
        }
        overtimeRequestPort.updateDepartment(requestId, departmentId);
        overtimeRequestPort.updateDetails(requestId, projectName, category);
        overtimeRequestPort.updateCorrection(requestId, correction, correctionMid);
        boolean autoApproved = approvalWorkflowService.startRoute(RequestType.OVERTIME, requestId, employeeId, departmentId, facts);
        if (autoApproved) {
            overtimeRequestPort.decide(requestId, true, employeeId);
        }
        return autoApproved;
    }

    /** 申請日と同じ月の、申請日以外の日の時間外の実績（分）。申請日の分は今回の申請の予定で数える */
    private int monthlyOvertimeMinutesExcept(EmployeeId id, LocalDate date) {
        if (loadAttendanceRecordPort == null) return 0;
        return loadAttendanceRecordPort.loadByEmployeeAndMonth(id, YearMonth.from(date)).stream()
                .filter(r -> !date.equals(r.getWorkDate()))
                .mapToInt(AttendanceRecord::getOvertimeMinutes)
                .sum();
    }

    /** 予定が深夜（22:00〜翌5:00）にかかるか：5時より前の出勤、22時より後の退勤、日をまたぐ退勤 */
    /** 休日（土日・祝日・会社休日）か */
    private boolean isHoliday(LocalDate date) {
        java.time.DayOfWeek d = date.getDayOfWeek();
        if (d == java.time.DayOfWeek.SATURDAY || d == java.time.DayOfWeek.SUNDAY
                || com.example.kintai.domain.service.JapaneseHolidayCalendar.isHoliday(date)) {
            return true;
        }
        return companyHolidayPort != null && companyHolidayPort.findCompanyHolidays(date, date).containsKey(date);
    }

    static boolean isNight(String plannedStartTime, String plannedEndTime) {
        if (plannedStartTime != null && !plannedStartTime.isBlank()
                && LocalTime.parse(plannedStartTime).isBefore(NIGHT_END)) {
            return true;
        }
        if (plannedEndTime == null || plannedEndTime.isBlank()) {
            return false;
        }
        return OvertimeRequest.isPlannedEndNextDay(plannedStartTime, plannedEndTime)
                || LocalTime.parse(plannedEndTime).isAfter(NIGHT_START);
    }

    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(5, 0);

    @Override
    public OvertimeRequest withdraw(String employeeId, int requestId) {
        OvertimeRequest request = overtimeRequestPort.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("取り下げる残業事前申請が見つかりません。画面を読み込み直してください。"));
        if (employeeId == null || !employeeId.equals(request.getEmployeeId())) {
            throw new IllegalArgumentException("自分の残業事前申請だけ取り下げられます。");
        }
        if (!request.isPending() || !overtimeRequestPort.withdraw(requestId)) {
            throw new IllegalStateException(request.getTargetDate() + " の申請は、もう" + request.getStatusLabel()
                    + "になっているため取り下げられません。");
        }
        return request;
    }

    @Override
    public List<OvertimeRequest> getMyRequests(String employeeId) {
        return overtimeRequestPort.findByEmployee(new EmployeeId(employeeId));
    }

    @Override
    public List<OvertimeRequest> getPending(String approverId) {
        return overtimeRequestPort.findPendingForApprover(approverId);
    }

    @Override
    public void decide(int requestId, boolean approve, String decidedBy) {
        OvertimeRequest request = overtimeRequestPort.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("対象の残業事前申請が見つかりません: " + requestId));
        if (!request.isPending()) {
            throw new IllegalStateException("申請中の残業事前申請のみ承認・却下できます。");
        }

        ApprovalOutcome outcome = approvalWorkflowService.decide(
                RequestType.OVERTIME, requestId, request.getCurrentLevel(), decidedBy, approve);
        switch (outcome) {
            case NEXT_LEVEL:
                overtimeRequestPort.advanceLevel(requestId);
                break;
            case APPROVED:
                overtimeRequestPort.decide(requestId, true, decidedBy);
                break;
            case REJECTED:
                overtimeRequestPort.decide(requestId, false, decidedBy);
                break;
        }
    }
}
