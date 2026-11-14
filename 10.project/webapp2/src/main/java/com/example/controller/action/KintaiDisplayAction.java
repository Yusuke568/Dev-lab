package com.example.controller.action;

import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetCorrectionMastersUseCase;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.port.in.GetMyOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.GetWeeklyWorkReportsUseCase;
import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;
import com.example.kintai.domain.model.attendance.CorrectionMaster;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.entity.WorkType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 勤怠入力画面を表示するアクションクラス。（リファクタリング後！E
 * 責務：リクエストを解釈し、E��刁E��ユースケースを呼び出し、結果をビューに渡す、E
 */
public class KintaiDisplayAction implements Action {

    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;
    private final GetCorrectionMastersUseCase getCorrectionMastersUseCase;
    private final GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase;
    private final GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase;
    private final com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase;
    private final com.example.kintai.application.port.in.GetWeeklyWorkSummariesUseCase getWeeklyWorkSummariesUseCase;
    private final com.example.kintai.application.port.in.GetHolidaysUseCase getHolidaysUseCase;
    private final com.example.notice.application.port.in.NoticeUseCase noticeUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();
    /** 週40時間の時間外を画面で計算するための、月の外の日（最初と最後の週）。null なら渡さない */
    private com.example.kintai.application.service.WeeklyOvertimeService weeklyOvertimeService;

    /** いつもの補正CD。null なら出さない */
    private com.example.kintai.domain.port.out.DefaultCorrectionPort defaultCorrectionPort;

    /** 承認者の「承認・確認待ち」の件数（画面右上のバッジ）。null なら出さない */
    private com.example.kintai.application.service.ApprovalBadgeService approvalBadgeService;

    public KintaiDisplayAction withApprovalBadge(com.example.kintai.application.service.ApprovalBadgeService service) {
        this.approvalBadgeService = service;
        return this;
    }

    /** 承認の権限がある人だけ、承認・確認待ちの件数を画面に渡す */
    private void setApprovalBadge(HttpServletRequest request, String loginUser) {
        if (approvalBadgeService == null || loginUser == null) return;
        javax.servlet.http.HttpSession session = request.getSession(false);
        Object features = session == null ? null : session.getAttribute("features");
        if (features instanceof java.util.Set && ((java.util.Set<?>) features).contains("APPROVAL")) {
            request.setAttribute("approvalBadge", approvalBadgeService.countFor(loginUser));
        }
    }

    /** 承認・却下の理由（勤怠の承認履歴）。null なら出さない */
    private com.example.kintai.domain.port.out.LoadApprovalHistoryPort approvalHistoryPort;
    /** 残業事前申請の承認・却下の理由。null なら出さない */
    private com.example.approval.application.service.ApprovalWorkflowService approvalWorkflowService;

    public KintaiDisplayAction withApprovalComments(com.example.kintai.domain.port.out.LoadApprovalHistoryPort historyPort,
                                                    com.example.approval.application.service.ApprovalWorkflowService workflow) {
        this.approvalHistoryPort = historyPort;
        this.approvalWorkflowService = workflow;
        return this;
    }

    /**
     * 日ごとの承認・却下の理由を画面に渡す。
     *   attendanceApprovalComments … 勤怠（有給・事前申請のない時間外）の、いちばん新しい判定の理由
     *   overtimeRequestComments    … 残業事前申請の、各段の理由（「山田：理由」を ／ でつなぐ）
     */
    private void setApprovalComments(HttpServletRequest request, String employeeId, YearMonth yearMonth,
                                     java.util.Map<LocalDate, OvertimeRequest> requestsByDate) {
        try {
            if (approvalHistoryPort != null) {
                request.setAttribute("attendanceApprovalComments",
                        approvalHistoryPort.findLatestComments(employeeId, yearMonth.atDay(1), yearMonth.atEndOfMonth()));
            }
            if (approvalWorkflowService != null && !requestsByDate.isEmpty()) {
                java.util.Map<Integer, LocalDate> dateOf = new java.util.HashMap<>();
                requestsByDate.forEach((d, r) -> {
                    if (YearMonth.from(d).equals(yearMonth)) dateOf.put(r.getId(), d);
                });
                java.util.Map<LocalDate, String> comments = new java.util.HashMap<>();
                approvalWorkflowService.getRoutes(com.example.approval.domain.model.RequestType.OVERTIME, dateOf.keySet())
                        .forEach((id, levels) -> {
                            List<String> parts = new java.util.ArrayList<>();
                            for (com.example.approval.domain.model.ApprovalLevel lv : levels) {
                                if (!lv.getComment().isEmpty()) {
                                    parts.add(lv.getDecided().getApproverName() + "：" + lv.getComment());
                                }
                            }
                            if (!parts.isEmpty()) comments.put(dateOf.get(id), String.join(" ／ ", parts));
                        });
                request.setAttribute("overtimeRequestComments", comments);
            }
        } catch (RuntimeException e) {
            // 理由が出せなくても勤怠表は出す
            java.util.logging.Logger.getLogger(KintaiDisplayAction.class.getName())
                    .log(java.util.logging.Level.WARNING, "承認・却下の理由を読めませんでした", e);
        }
    }

    public KintaiDisplayAction withDefaultCorrection(com.example.kintai.domain.port.out.DefaultCorrectionPort port) {
        this.defaultCorrectionPort = port;
        return this;
    }

    public KintaiDisplayAction withWeeklyOvertime(com.example.kintai.application.service.WeeklyOvertimeService service) {
        this.weeklyOvertimeService = service;
        return this;
    }

    /**
     * コンストラクタ、E
     * 依存性は外部のファクトリ�E�EIコンチE���E�から注入されます、E
     */
    public KintaiDisplayAction(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase, GetWorkTypesUseCase getWorkTypesUseCase,
                                GetCorrectionMastersUseCase getCorrectionMastersUseCase, GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase,
                                GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase,
                                com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase,
                                com.example.kintai.application.port.in.GetWeeklyWorkSummariesUseCase getWeeklyWorkSummariesUseCase,
                                com.example.kintai.application.port.in.GetHolidaysUseCase getHolidaysUseCase,
                                com.example.notice.application.port.in.NoticeUseCase noticeUseCase) {
        this.getHolidaysUseCase = getHolidaysUseCase;
        this.noticeUseCase = noticeUseCase;
        this.attendanceAccessUseCase = attendanceAccessUseCase;
        this.getWeeklyWorkSummariesUseCase = getWeeklyWorkSummariesUseCase;
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
        this.getCorrectionMastersUseCase = getCorrectionMastersUseCase;
        this.getMyOvertimeRequestsUseCase = getMyOvertimeRequestsUseCase;
        this.getWeeklyWorkReportsUseCase = getWeeklyWorkReportsUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 1. リクエストからコマンドを作�E
            // 表示できるのは、本人の勤怠表と、承認権限を持つ人にとっての組織図上の部下の社員の勤怠表
            HttpSession loginSession = request.getSession(false);
            String loginUser = loginSession != null ? (String) loginSession.getAttribute("loginUser") : null;
            if (loginUser == null) {
                return new View("/login.do", true);
            }
            String requestedId = request.getParameter("id");
            String idParam = requestedId == null || requestedId.isBlank() ? loginUser : requestedId.trim();
            boolean proxyMode = !idParam.equals(loginUser);
            if (proxyMode) {
                @SuppressWarnings("unchecked")
                Set<String> features = (Set<String>) loginSession.getAttribute("features");
                if (!attendanceAccessUseCase.canAccess(loginUser, features, idParam)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    request.setAttribute("errorMessage", "この社員の勤怠表を見る権限がありません。組織図で自分の部下の勤怠表だけを表示できます。");
                    return new View("/WEB-INF/view/error.jsp");
                }
            }
            request.setAttribute("proxyMode", proxyMode);
            // 上長からのお知らせ（画面を見ている本人あて）
            request.setAttribute("notices", noticeUseCase.getNoticesFor(loginUser));
            // 休憩時間帯（画面で勤務時間を計算し直すときに、サーバーと同じ休憩を引くため）
            request.setAttribute("breakPeriodsJson", com.example.kintai.domain.model.attendance.BreakSchedule.toJson());
            // 祝日と会社休日（行の色と名前の表示用）
            request.setAttribute("holidayNames", getHolidaysUseCase.getHolidays(parseYearMonth(
                    request.getParameter("year"), request.getParameter("month"))));
            String yearParam = request.getParameter("year");
            String monthParam = request.getParameter("month");

            // ログイン中のユーザー惁E��をセチE��ョンから取得する方が望ましいが、ここではパラメータを維持E
            EmployeeId employeeId = new EmployeeId(idParam);
            YearMonth yearMonth = parseYearMonth(yearParam, monthParam);
            
            GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command =
                    new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth);

            // 2. ユースケースを実衁E
            MonthlyAttendanceDto attendanceData = getMonthlyAttendanceUseCase.getMonthlyAttendance(command);
            List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();
            String workTypesJson = objectMapper.writeValueAsString(workTypes);
            List<CorrectionMaster> correctionMasters = getCorrectionMastersUseCase.getCorrectionMasters();

            // 残業事前申請の状況（ステータス通知用）：申請済みの対象日、申請中・却下の件数
            List<OvertimeRequest> myRequests = getMyOvertimeRequestsUseCase.getMyRequests(idParam);
            Set<LocalDate> requestedDates = myRequests.stream()
                    .map(OvertimeRequest::getTargetDate)
                    .collect(Collectors.toSet());
            List<LocalDate> datesNeedingRequest = attendanceData.getDailyRecords().stream()
                    .filter(d -> d.getOvertimeMinutes() > 0 && !requestedDates.contains(d.getDate()))
                    .map(DailyAttendanceDto::getDate)
                    .collect(Collectors.toList());
            // 日ごとの残業事前申請の状態（勤怠表の「状態」欄用）。同じ日に複数あれば後のものを使う
            java.util.Map<LocalDate, String> overtimeRequestStatus = myRequests.stream()
                    .collect(Collectors.toMap(OvertimeRequest::getTargetDate, OvertimeRequest::getStatus, (a, b) -> b));
            request.setAttribute("overtimeRequestStatus", overtimeRequestStatus);
            // 日ごとの申請（取り下げは除く）。勤怠表・カレンダーの申請アイコンと「予定超過」に使う
            java.util.Map<LocalDate, OvertimeRequest> overtimeRequestsByDate = myRequests.stream()
                    .filter(r -> !r.isWithdrawn())
                    .collect(Collectors.toMap(OvertimeRequest::getTargetDate, r -> r, (a, b) -> b));
            request.setAttribute("overtimeRequestsByDate", overtimeRequestsByDate);
            setApprovalComments(request, idParam, yearMonth, overtimeRequestsByDate);
            long pendingRequestCount = myRequests.stream().filter(OvertimeRequest::isPending).count();
            long rejectedRequestCount = myRequests.stream().filter(OvertimeRequest::isRejected).count();

            // 3. 結果をリクエストスコープに設宁E
            request.setAttribute("attendanceData", attendanceData);
            request.setAttribute("workTypes", workTypes);
            request.setAttribute("workTypesJson", workTypesJson);
            request.setAttribute("correctionMasters", correctionMasters);
            request.setAttribute("datesNeedingRequest", datesNeedingRequest);
            request.setAttribute("pendingRequestCount", pendingRequestCount);
            request.setAttribute("rejectedRequestCount", rejectedRequestCount);
            request.setAttribute("staffId", idParam);
            setApprovalBadge(request, loginUser);
            // いつもの補正CD（表示している社員の。未設定の日に入力を始めたときの初期値）
            if (defaultCorrectionPort != null) {
                try {
                    request.setAttribute("defaultCorrectionId", defaultCorrectionPort.findDefaultCorrectionId(idParam));
                } catch (RuntimeException e) {
                    e.printStackTrace(); // 列がない（マイグレーション前）などでも勤怠は開けるようにする
                }
            }

            // 月遷移（前月・次月）リンク用
            YearMonth prevMonth = yearMonth.minusMonths(1);
            YearMonth nextMonth = yearMonth.plusMonths(1);
            request.setAttribute("prevYear", prevMonth.getYear());
            request.setAttribute("prevMonth", prevMonth.getMonthValue());
            request.setAttribute("nextYear", nextMonth.getYear());
            request.setAttribute("nextMonth", nextMonth.getMonthValue());

            // 週次作業報告（作業報告書Excel出力の「作業件名」「作業進捗」「本人所感」欄に対応）
            List<WeeklyWorkReport> weeklyWorkReports = getWeeklyWorkReportsUseCase.getWeeklyWorkReports(employeeId, yearMonth);
            request.setAttribute("weeklyWorkReports", weeklyWorkReports);
            // 各週の「累計/残業時間」（作業報告書Excelでは数式で計算される欄。画面では確認用に表示する）
            request.setAttribute("weeklyWorkSummaries", getWeeklyWorkSummariesUseCase.getWeeklyWorkSummaries(employeeId, yearMonth));
            // 週40時間：月の最初と最後の週の、表にない日（前月・翌月）の勤務。画面の時間外の計算に使う
            java.util.List<java.util.Map<String, Object>> outsideDays = new java.util.ArrayList<>();
            if (weeklyOvertimeService != null) {
                for (com.example.kintai.domain.service.WeeklyOvertimeCalculator.Day day
                        : weeklyOvertimeService.outsideMonthDays(employeeId, yearMonth)) {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("date", day.date().toString());
                    m.put("total", day.totalMinutes());
                    m.put("holiday", day.holiday());
                    m.put("leave", day.leaveCreditMinutes());
                    outsideDays.add(m);
                }
            }
            request.setAttribute("outsideWeekDaysJson", objectMapper.writeValueAsString(outsideDays));

            // 作業報告書Excelインポート等、他アクションからのセッション経由メッセージ
            HttpSession messageSession = request.getSession(false);
            if (messageSession != null) {
                Object successMessage = messageSession.getAttribute("kintaiSuccessMessage");
                if (successMessage != null) {
                    request.setAttribute("kintaiSuccessMessage", successMessage);
                    messageSession.removeAttribute("kintaiSuccessMessage");
                }
                Object importErrorMessage = messageSession.getAttribute("kintaiErrorMessage");
                if (importErrorMessage != null) {
                    request.setAttribute("kintaiErrorMessage", importErrorMessage);
                    messageSession.removeAttribute("kintaiErrorMessage");
                }
            }

            // 4. ビューにフォワーチE
            return new View("/WEB-INF/view/kintai.jsp");

        } catch (Exception e) {
            // エラーハンドリングを強化することが望ましい
            e.printStackTrace();
            request.setAttribute("errorMessage", "勤怠惁E��の表示中にエラーが発生しました: " + e.getMessage());
            return new View("/WEB-INF/view/error.jsp");
        }
    }

    /**
     * 表示する年月。URL に年・月がない（ブックマークや直接入力など）か正しくなければ、今月にする。
     */
    static YearMonth parseYearMonth(String yearParam, String monthParam) {
        try {
            int year = Integer.parseInt(yearParam.trim());
            int month = Integer.parseInt(monthParam.trim());
            if (year >= 2000 && year <= 2100 && month >= 1 && month <= 12) {
                return YearMonth.of(year, month);
            }
        } catch (RuntimeException e) {
            // 年・月がない、または数字でないときは今月
        }
        return YearMonth.now();
    }
}
