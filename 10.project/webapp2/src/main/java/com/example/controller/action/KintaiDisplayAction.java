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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * コンストラクタ、E
     * 依存性は外部のファクトリ�E�EIコンチE���E�から注入されます、E
     */
    public KintaiDisplayAction(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase, GetWorkTypesUseCase getWorkTypesUseCase,
                                GetCorrectionMastersUseCase getCorrectionMastersUseCase, GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase,
                                GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase,
                                com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase,
                                com.example.kintai.application.port.in.GetWeeklyWorkSummariesUseCase getWeeklyWorkSummariesUseCase) {
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
            // 表示できるのは、本人の勤怠表と、承認権限を持つ人にとっての組織図上の配下の社員の勤怠表
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
                    request.setAttribute("errorMessage", "この社員の勤怠表を見る権限がありません。組織図で自分の配下にいる社員の勤怠表だけを表示できます。");
                    return new View("/WEB-INF/view/error.jsp");
                }
            }
            request.setAttribute("proxyMode", proxyMode);
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
