package controller.action;

import controller.Action;
import controller.View;
import domain.shain.Shain;
import usecase.shain.ShainService;
import domain.attendance.AbstractItem;
import domain.attendance.AttendanceRepository;
import domain.attendance.DailyAttendance;
import domain.attendance.LeaveType;
import domain.auth.AuthRepository;
import domain.auth.UserSession;
import infrastructure.persistence.JdbcAuthRepository;
import domain.compliance.Article36ComplianceService;
import domain.compliance.OvertimeLevel;
import domain.compliance.PaidLeaveComplianceService;
import domain.contract.Assignment;
import domain.contract.ContractRepository;
import domain.contract.ProjectContract;
import domain.contract.SettlementResult;
import domain.contract.SettlementService;
import infrastructure.persistence.JdbcAttendanceRepository;
import infrastructure.persistence.JdbcContractRepository;
import infrastructure.persistence.JdbcWeeklyReportRepository;
import domain.weeklyreport.WeeklyReport;
import domain.weeklyreport.WeeklyReportRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SES勤怠・精算・週報 統合表示アクション（認証・権限制御付き）
 */
public class SesAttendanceAction implements Action {

    private final AttendanceRepository attendanceRepo = new JdbcAttendanceRepository();
    private final ContractRepository contractRepo = new JdbcContractRepository();
    private final WeeklyReportRepository weeklyRepo = new JdbcWeeklyReportRepository();
    private final AuthRepository authRepo = new JdbcAuthRepository();
    private final SettlementService settlementService = new SettlementService();
    private final Article36ComplianceService article36Service = new Article36ComplianceService();
    private final PaidLeaveComplianceService paidLeaveService = new PaidLeaveComplianceService();

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 認証チェック
        HttpSession session = request.getSession(false);
        UserSession userSession = session != null ? (UserSession) session.getAttribute("userSession") : null;
        if (userSession == null) {
            return new View("/login.do", true);
        }

        // 2. 権限に基づく表示対象社員IDの決定
        String requestedEmployeeId = request.getParameter("employeeId");
        String targetEmployeeId;

        if (userSession.canManageOthers() && requestedEmployeeId != null && !requestedEmployeeId.trim().isEmpty()) {
            targetEmployeeId = requestedEmployeeId.trim();
        } else {
            // 一般メンバーは常に自分自身のデータのみ
            targetEmployeeId = userSession.getEmployeeId();
        }

        String yearMonthStr = request.getParameter("yearMonth");
        YearMonth yearMonth = parseYearMonth(yearMonthStr);

        // 3. 契約・アサイン情報
        Assignment assignment = contractRepo.findActiveAssignment(targetEmployeeId).orElse(null);
        ProjectContract contract = null;
        if (assignment != null) {
            contract = contractRepo.findContractById(assignment.getProjectContractId()).orElse(null);
        }

        // 4. 日次勤怠一覧（土日・祝日を含め、月内全日を補完表示）
        List<DailyAttendance> dbAttendances = attendanceRepo.findByEmployeeAndMonth(targetEmployeeId, yearMonth);
        Map<LocalDate, DailyAttendance> attMap = new HashMap<>();
        for (DailyAttendance a : dbAttendances) {
            attMap.put(a.getWorkDate(), a);
        }

        List<DailyAttendance> attendances = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            DailyAttendance att = attMap.get(date);
            if (att != null) {
                attendances.add(att);
            } else {
                String recordId = String.format("%s_%s", targetEmployeeId, date.toString());
                boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
                if (isWeekend) {
                    attendances.add(DailyAttendance.createHoliday(recordId, targetEmployeeId, date));
                } else if (contract != null) {
                    attendances.add(DailyAttendance.createFromTemplate(
                            recordId, targetEmployeeId, date,
                            contract.getDefaultStartTime(),
                            contract.getDefaultEndTime(),
                            contract.getDefaultBreakMinutes()
                    ));
                } else {
                    attendances.add(DailyAttendance.createHoliday(recordId, targetEmployeeId, date));
                }
            }
        }

        // 5. 稼働時間集計 & 精算判定
        int totalDirectMinutes = attendances.stream().filter(DailyAttendance::isFilled).mapToInt(DailyAttendance::getDirectMinutes).sum();
        int totalIndirectMinutes = attendances.stream().filter(DailyAttendance::isFilled).mapToInt(DailyAttendance::getIndirectMinutes).sum();
        int totalWorkMinutes = attendances.stream().filter(DailyAttendance::isFilled).mapToInt(DailyAttendance::calculateTotalWorkMinutes).sum();
        int legalOvertimeMinutes = attendances.stream().filter(DailyAttendance::isFilled).mapToInt(a -> Math.max(0, a.calculateTotalWorkMinutes() - 480)).sum();

        SettlementResult settlementResult = null;
        if (contract != null && !attendances.isEmpty()) {
            settlementResult = settlementService.calculate(contract, 20, 20, totalDirectMinutes, assignment.getAllocationRatio());
        }

        // 6. 有休残日数および36協定・有休コンプライアンス判定
        int staffPaidLeaveDays = 20;
        try {
            ShainService shainService = new ShainService();
            Shain shain = shainService.getShainById(Integer.parseInt(targetEmployeeId));
            if (shain != null) {
                staffPaidLeaveDays = shain.getPaidLeaveDays();
            }
        } catch (Exception ignored) {}

        double monthPaidDays = attendances.stream().mapToDouble(a -> {
            if (a.getAbstractId() == 3 || a.getLeaveType() == LeaveType.PAID_LEAVE) return 1.0;
            if (a.getAbstractId() == 5 || a.getLeaveType() == LeaveType.HALF_PAID_AM || a.getLeaveType() == LeaveType.HALF_PAID_PM) return 0.5;
            return 0.0;
        }).sum();
        double remainingPaidDays = Math.max(0.0, staffPaidLeaveDays - monthPaidDays);

        OvertimeLevel overtimeLevel = article36Service.evaluateMonthlyOvertime(legalOvertimeMinutes);
        String paidLeaveWarning = paidLeaveService.checkPaidLeavePace(6, monthPaidDays);

        // 7. 週報（月内の週リスト & 選択された週の取得）
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int defaultWeekNum = today.get(weekFields.weekOfWeekBasedYear());
        String defaultYearWeek = String.format("%d-W%02d", today.getYear(), defaultWeekNum);

        List<String> monthWeeks = new ArrayList<>();
        LocalDate cur = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        while (!cur.isAfter(monthEnd)) {
            int w = cur.get(weekFields.weekOfWeekBasedYear());
            int y = cur.get(weekFields.weekBasedYear());
            String yw = String.format("%d-W%02d", y, w);
            if (!monthWeeks.contains(yw)) {
                monthWeeks.add(yw);
            }
            cur = cur.plusDays(7);
        }
        if (monthWeeks.isEmpty()) {
            monthWeeks.add(defaultYearWeek);
        }

        String requestedYearWeek = request.getParameter("yearWeek");
        String currentYearWeek;
        if (requestedYearWeek != null && !requestedYearWeek.trim().isEmpty()) {
            currentYearWeek = requestedYearWeek.trim();
        } else if (monthWeeks.contains(defaultYearWeek)) {
            currentYearWeek = defaultYearWeek;
        } else {
            currentYearWeek = monthWeeks.get(0);
        }

        WeeklyReport currentWeeklyReport = weeklyRepo.findByEmployeeAndWeek(targetEmployeeId, currentYearWeek).orElse(null);
        WeeklyReport previousWeeklyReport = weeklyRepo.findLatestBefore(targetEmployeeId, currentYearWeek).orElse(null);

        // 8. 摘要マスタ一覧取得
        List<AbstractItem> abstractItems = attendanceRepo.findAllAbstractItems();

        // 9. 管理者・上司用：全社員リスト取得
        List<UserSession> allStaff = null;
        if (userSession.canManageOthers()) {
            allStaff = authRepo.findAllStaff();
        }

        // リクエストスコープ設定
        request.setAttribute("userSession", userSession);
        request.setAttribute("targetEmployeeId", targetEmployeeId);
        request.setAttribute("allStaff", allStaff);
        request.setAttribute("yearMonth", yearMonth.toString());
        request.setAttribute("prevMonth", yearMonth.minusMonths(1).toString());
        request.setAttribute("nextMonth", yearMonth.plusMonths(1).toString());
        request.setAttribute("contract", contract);
        request.setAttribute("assignment", assignment);
        request.setAttribute("attendances", attendances);
        request.setAttribute("abstractItems", abstractItems);
        request.setAttribute("staffPaidLeaveDays", staffPaidLeaveDays);
        request.setAttribute("monthPaidDays", monthPaidDays);
        request.setAttribute("remainingPaidDays", remainingPaidDays);
        request.setAttribute("totalDirectMinutes", totalDirectMinutes);
        request.setAttribute("totalIndirectMinutes", totalIndirectMinutes);
        request.setAttribute("totalWorkMinutes", totalWorkMinutes);
        request.setAttribute("legalOvertimeMinutes", legalOvertimeMinutes);
        request.setAttribute("settlementResult", settlementResult);
        request.setAttribute("overtimeLevel", overtimeLevel);
        request.setAttribute("paidLeaveWarning", paidLeaveWarning);
        request.setAttribute("monthWeeks", monthWeeks);
        request.setAttribute("currentYearWeek", currentYearWeek);
        request.setAttribute("currentWeeklyReport", currentWeeklyReport);
        request.setAttribute("previousWeeklyReport", previousWeeklyReport);

        return new View("/WEB-INF/view/ses_attendance.jsp");
    }

    private YearMonth parseYearMonth(String input) {
        if (input == null || input.trim().isEmpty()) {
            return YearMonth.now();
        }
        String s = input.trim();
        String[] parts = s.split("[-/]");
        if (parts.length == 2) {
            try {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                return YearMonth.of(year, month);
            } catch (Exception ignored) {
            }
        }
        try {
            return YearMonth.parse(s);
        } catch (Exception e) {
            return YearMonth.now();
        }
    }
}
