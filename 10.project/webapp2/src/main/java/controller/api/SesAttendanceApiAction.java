package controller.api;

import controller.Action;
import controller.View;
import domain.attendance.*;
import usecase.attendance.ApplyMonthlyTemplateUseCase;
import usecase.attendance.FillAttendanceUseCase;
import domain.auth.UserRole;
import domain.auth.UserSession;
import domain.contract.ContractRepository;
import domain.contract.ProjectContract;
import domain.contract.Assignment;
import domain.contract.SettlementService;
import usecase.contract.CalculateSettlementUseCase;
import infrastructure.persistence.JdbcAttendanceRepository;
import infrastructure.persistence.JdbcContractRepository;
import infrastructure.persistence.JdbcWeeklyReportRepository;
import domain.weeklyreport.AuthorRole;
import domain.weeklyreport.WeeklyReportRepository;
import usecase.weeklyreport.ReviewWeeklyReportUseCase;
import usecase.weeklyreport.SubmitWeeklyReportUseCase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
 * SES勤怠・精算・週報 非同期APIアクション（認証・認可制御付き）
 */
public class SesAttendanceApiAction implements Action {

    private final AttendanceRepository attendanceRepo = new JdbcAttendanceRepository();
    private final ContractRepository contractRepo = new JdbcContractRepository();
    private final WeeklyReportRepository weeklyRepo = new JdbcWeeklyReportRepository();
    private final AttendanceTemplateService templateService = new AttendanceTemplateService();

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        // 1. 認証チェック
        HttpSession session = request.getSession(false);
        UserSession userSession = session != null ? (UserSession) session.getAttribute("userSession") : null;
        if (userSession == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "ログインセッションが切れました。再ログインしてください。");
            response.getWriter().write(toJson(result));
            return null;
        }

        String actionType = request.getParameter("actionType");
        String requestedEmployeeId = request.getParameter("employeeId");
        String targetEmployeeId = (requestedEmployeeId != null && !requestedEmployeeId.trim().isEmpty())
                ? requestedEmployeeId.trim()
                : userSession.getEmployeeId();

        // 2. 認可チェック（一般メンバーは自分以外の編集・提出不可、管理者アクションは管理層のみ）
        boolean isMgmtAction = "addSupervisorReview".equals(actionType)
                || "approveFinal".equals(actionType)
                || "remandReport".equals(actionType)
                || "addPrivateNote".equals(actionType);

        if (!userSession.canEdit(targetEmployeeId) && !isMgmtAction) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "他の社員の勤怠・週報を操作する権限がありません。");
            response.getWriter().write(toJson(result));
            return null;
        }

        if (isMgmtAction && !userSession.canManageOthers()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "管理・承認権限がありません。");
            response.getWriter().write(toJson(result));
            return null;
        }

        try {
            if ("applyTemplate".equals(actionType)) {
                // 1. 月初テンプレート自動展開
                YearMonth ym = parseYearMonth(request.getParameter("yearMonth"));
                ApplyMonthlyTemplateUseCase useCase = new ApplyMonthlyTemplateUseCase(attendanceRepo, contractRepo, templateService);
                List<DailyAttendance> templates = useCase.execute(targetEmployeeId, ym);
                result.put("success", true);
                result.put("message", templates.size() + "日分の勤務テンプレートを展開しました。");

            } else if ("fillAttendance".equals(actionType)) {
                // 2. 日次勤怠事後差分入力（摘要プルダウン連動）
                LocalDate workDate = LocalDate.parse(request.getParameter("workDate"));
                LocalTime startTime = LocalTime.parse(request.getParameter("startTime"));
                LocalTime endTime = LocalTime.parse(request.getParameter("endTime"));
                int breakMinutes = Integer.parseInt(request.getParameter("breakMinutes"));

                int abstractId = 1;
                String abstractIdParam = request.getParameter("abstractId");
                if (abstractIdParam != null && !abstractIdParam.trim().isEmpty()) {
                    abstractId = Integer.parseInt(abstractIdParam.trim());
                }

                LeaveType leaveType = LeaveType.NORMAL;
                if (abstractId == 3) {
                    leaveType = LeaveType.PAID_LEAVE;
                } else if (abstractId == 5) {
                    leaveType = LeaveType.HALF_PAID_AM;
                } else if (abstractId == 6) {
                    leaveType = LeaveType.SPECIAL_LEAVE;
                } else if (abstractId == 4 || abstractId == 8) {
                    leaveType = LeaveType.SUBSTITUTE_HOLIDAY;
                }

                String remarks = request.getParameter("remarks");
                int directMinutes = Integer.parseInt(request.getParameter("directMinutes"));
                int indirectMinutes = Integer.parseInt(request.getParameter("indirectMinutes"));
                String indirectTask = request.getParameter("indirectTask");

                List<WorkTimeSegment> segments = new ArrayList<>();
                if (directMinutes > 0) {
                    segments.add(new WorkTimeSegment(targetEmployeeId + "_" + workDate + "_dir", WorkCategory.DIRECT, directMinutes, "客先直接業務"));
                }
                if (indirectMinutes > 0) {
                    segments.add(new WorkTimeSegment(targetEmployeeId + "_" + workDate + "_indir", WorkCategory.INDIRECT, indirectMinutes, indirectTask != null ? indirectTask : "社内間接業務"));
                }

                FillAttendanceUseCase useCase = new FillAttendanceUseCase(attendanceRepo);
                DailyAttendance att = useCase.execute(new FillAttendanceUseCase.Input(
                        targetEmployeeId, workDate, startTime, endTime, breakMinutes, abstractId, leaveType, remarks, segments
                ));

                result.put("success", true);
                result.put("message", workDate + " の勤怠を登録しました。");
                result.put("directMinutes", att.getDirectMinutes());
                result.put("indirectMinutes", att.getIndirectMinutes());

            } else if ("clearAttendance".equals(actionType)) {
                // 2-B. 日次勤怠の入力クリア・リセット
                LocalDate workDate = LocalDate.parse(request.getParameter("workDate"));
                DailyAttendance att = attendanceRepo.findByEmployeeAndDate(targetEmployeeId, workDate).orElse(null);
                if (att != null) {
                    ProjectContract contract = null;
                    Assignment assignment = contractRepo.findActiveAssignment(targetEmployeeId).orElse(null);
                    if (assignment != null) {
                        contract = contractRepo.findContractById(assignment.getProjectContractId()).orElse(null);
                    }
                    att.reset(contract);
                    attendanceRepo.save(att);
                }
                result.put("success", true);
                result.put("message", workDate + " の勤怠入力をクリアしました。");

            } else if ("submitWeeklyReport".equals(actionType)) {
                // 3. 週報提出（3分割入力＋日次未入力チェック）
                String yearWeek = request.getParameter("yearWeek");
                LocalDate submitDate = LocalDate.now();
                String taskTitle = request.getParameter("taskTitle");
                String taskProgress = request.getParameter("taskProgress");
                String personalReflection = request.getParameter("personalReflection");

                SubmitWeeklyReportUseCase useCase = new SubmitWeeklyReportUseCase(
                        weeklyRepo, attendanceRepo, templateService, null
                );
                useCase.execute(new SubmitWeeklyReportUseCase.Input(
                        targetEmployeeId + "_" + yearWeek, targetEmployeeId, yearWeek, submitDate,
                        taskTitle, taskProgress, personalReflection
                ));

                result.put("success", true);
                result.put("message", yearWeek + " の週報を提出しました。");

            } else if ("skipWeeklyReport".equals(actionType)) {
                // 4. 週報スキップ（GW・長期休暇等）
                String yearWeek = request.getParameter("yearWeek");
                String reason = request.getParameter("reason");
                ReviewWeeklyReportUseCase useCase = new ReviewWeeklyReportUseCase(weeklyRepo);
                useCase.skipWeek(targetEmployeeId + "_" + yearWeek, targetEmployeeId, yearWeek, reason);

                result.put("success", true);
                result.put("message", yearWeek + " の週報提出をスキップ登録しました。");

            } else if ("addSupervisorReview".equals(actionType)) {
                // 5. 課長所見の登録（一次レビュー）
                String reportId = request.getParameter("reportId");
                String comment = request.getParameter("comment");
                if (comment == null || comment.trim().isEmpty()) {
                    throw new IllegalArgumentException("課長所見を入力してください。");
                }

                ReviewWeeklyReportUseCase useCase = new ReviewWeeklyReportUseCase(weeklyRepo);
                useCase.addSupervisorReview(reportId, userSession.getEmployeeId(), comment.trim(), false);

                result.put("success", true);
                result.put("message", "課長所見を登録しました。所属長承認待ちになりました。");

            } else if ("approveFinal".equals(actionType)) {
                // 6. 所属長所見の登録（最終承認：ADMINのみ）
                if (userSession.getRole() != UserRole.ADMIN) {
                    throw new IllegalStateException("所属長所見・最終承認は部長/管理者（ADMIN）権限が必要です。");
                }
                String reportId = request.getParameter("reportId");
                String comment = request.getParameter("comment");

                ReviewWeeklyReportUseCase useCase = new ReviewWeeklyReportUseCase(weeklyRepo);
                useCase.approveFinal(reportId, userSession.getEmployeeId(), comment != null ? comment.trim() : "");

                result.put("success", true);
                result.put("message", "所属長所見を登録し、最終承認を完了しました。");

            } else if ("remandReport".equals(actionType)) {
                // 7. 週報差戻し
                String reportId = request.getParameter("reportId");
                String reason = request.getParameter("reason");
                if (reason == null || reason.trim().isEmpty()) {
                    throw new IllegalArgumentException("差戻し理由を入力してください。");
                }

                AuthorRole role = userSession.getRole() == UserRole.ADMIN ? AuthorRole.FINAL_APPROVER : AuthorRole.SUPERVISOR;
                ReviewWeeklyReportUseCase useCase = new ReviewWeeklyReportUseCase(weeklyRepo);
                useCase.remand(reportId, userSession.getEmployeeId(), role, reason.trim());

                result.put("success", true);
                result.put("message", "週報を差戻しました。");

            } else if ("addPrivateNote".equals(actionType)) {
                // 8. 管理者共有メモ（本人非公開）の登録
                String reportId = request.getParameter("reportId");
                String note = request.getParameter("note");
                if (note == null || note.trim().isEmpty()) {
                    throw new IllegalArgumentException("管理共有メモを入力してください。");
                }

                AuthorRole role = userSession.getRole() == UserRole.ADMIN ? AuthorRole.FINAL_APPROVER : AuthorRole.SUPERVISOR;
                ReviewWeeklyReportUseCase useCase = new ReviewWeeklyReportUseCase(weeklyRepo);
                useCase.addManagementPrivateNote(reportId, userSession.getEmployeeId(), role, note.trim());

                result.put("success", true);
                result.put("message", "管理者共有メモ（本人非公開）を保存しました。");

            } else if ("calculateSettlement".equals(actionType)) {
                // 9. 月次稼働時間精算集計
                YearMonth ym = parseYearMonth(request.getParameter("yearMonth"));
                CalculateSettlementUseCase useCase = new CalculateSettlementUseCase(attendanceRepo, contractRepo, new SettlementService());
                CalculateSettlementUseCase.Output output = useCase.execute(targetEmployeeId, ym, 20, 20);

                result.put("success", true);
                result.put("message", "月次精算時間を集計しました。");
                result.put("directMinutes", output.monthlySummary.getDirectMinutes());
                result.put("settlementStatus", output.settlementResult.getStatus().name());
                result.put("diffHours", output.settlementResult.getDiffHours());

            } else {
                result.put("success", false);
                result.put("message", "未知のアクション種別です: " + actionType);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "処理エラー: " + e.getMessage());
        }

        response.getWriter().write(toJson(result));
        return null;
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object val = entry.getValue();
            if (val == null) {
                sb.append("null");
            } else if (val instanceof Boolean || val instanceof Number) {
                sb.append(val);
            } else {
                sb.append("\"").append(escapeJson(val.toString())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
