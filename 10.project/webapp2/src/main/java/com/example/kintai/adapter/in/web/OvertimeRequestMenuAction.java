package com.example.kintai.adapter.in.web;

import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.port.in.GetMyOvertimeRequestsUseCase;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 残業事前申請メニュー（自分の申請一覧・新規申請フォーム）を表示するアクション。
 * 各申請の対象日について、実際の勤怠実績（出退勤・実働時間）もあわせて表示（同期）する。
 *
 * kintai画面の通知から targetDate 付きで遷移してきた場合は、その日の実績（時間外分・退勤時刻）を
 * 申請フォームへ初期入力する。
 */
public class OvertimeRequestMenuAction implements Action {

    private final GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase;
    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    /** 兼任している人に、どの部署の業務としての申請かを選んでもらうため */
    private final com.example.approval.application.service.ApprovalWorkflowService approvalWorkflowService;

    public OvertimeRequestMenuAction(GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase,
                                      GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase,
                                      com.example.approval.application.service.ApprovalWorkflowService approvalWorkflowService) {
        this.getMyOvertimeRequestsUseCase = getMyOvertimeRequestsUseCase;
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    /** 残業の実績確認の進みを申請履歴に出す。null なら出さない */
    private com.example.kintai.application.service.OvertimeActualCheckService actualCheckService;

    public OvertimeRequestMenuAction withActualCheck(com.example.kintai.application.service.OvertimeActualCheckService service) {
        this.actualCheckService = service;
        return this;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;
        if (employeeId == null) {
            return new View("/login.do", true);
        }

        LocalDate prefillTargetDate = parseDateParam(request.getParameter("targetDate"));

        List<OvertimeRequest> myRequests = getMyOvertimeRequestsUseCase.getMyRequests(employeeId);

        // 各申請の対象日、および初期入力対象日の実際の勤怠実績を月ごとにまとめて取得する（勤怠情報との同期表示・初期入力）
        Set<YearMonth> months = new HashSet<>();
        for (OvertimeRequest r : myRequests) {
            months.add(YearMonth.from(r.getTargetDate()));
        }
        if (prefillTargetDate != null) {
            months.add(YearMonth.from(prefillTargetDate));
        }

        Map<LocalDate, DailyAttendanceDto> attendanceByDate = new HashMap<>();
        for (YearMonth ym : months) {
            GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command =
                    new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(new EmployeeId(employeeId), ym);
            MonthlyAttendanceDto monthly = getMonthlyAttendanceUseCase.getMonthlyAttendance(command);
            for (DailyAttendanceDto daily : monthly.getDailyRecords()) {
                attendanceByDate.put(daily.getDate(), daily);
            }
        }

        request.setAttribute("myRequests", myRequests);
        // 申請ごとの承認の進み（申請者が、誰のところで止まっているか・誰がいつ承認／却下したか・理由を見られるように）
        List<Integer> requestIds = new java.util.ArrayList<>();
        for (OvertimeRequest r : myRequests) requestIds.add(r.getId());
        request.setAttribute("overtimeRoutes",
                approvalWorkflowService.getRoutes(com.example.approval.domain.model.RequestType.OVERTIME, requestIds));
        if (actualCheckService != null) {
            request.setAttribute("actualCheckRoutes", actualCheckService.routesFor(requestIds));
        }
        setPreviousDetails(request, myRequests);
        request.setAttribute("myDepartments", approvalWorkflowService.getDepartmentsOfStaff(Integer.parseInt(employeeId)));
        request.setAttribute("attendanceByDate", attendanceByDate);

        if (prefillTargetDate != null) {
            request.setAttribute("prefillDate", prefillTargetDate.toString());
            DailyAttendanceDto actual = attendanceByDate.get(prefillTargetDate);
            if (actual != null) {
                if (!"-".equals(actual.getStartTime())) {
                    request.setAttribute("prefillStartTime", actual.getStartTime());
                }
                if (!"-".equals(actual.getEndTime())) {
                    request.setAttribute("prefillEndTime", actual.getEndTime());
                }
                // 勤怠に入っている補正も入れる（申請の時間外に足す）
                request.setAttribute("prefillCorrection", hhmm(actual.getCorrectionUsTime()));
                request.setAttribute("prefillCorrectionMid", hhmm(actual.getCorrectionMidTime()));
            }
        }

        HttpSession sessionForError = request.getSession(false);
        if (sessionForError != null && sessionForError.getAttribute("overtimeRequestErrorMessage") != null) {
            request.setAttribute("errorMessage", sessionForError.getAttribute("overtimeRequestErrorMessage"));
            sessionForError.removeAttribute("overtimeRequestErrorMessage");
        }
        if (sessionForError != null && sessionForError.getAttribute("overtimeRequestSuccessMessage") != null) {
            request.setAttribute("successMessage", sessionForError.getAttribute("overtimeRequestSuccessMessage"));
            sessionForError.removeAttribute("overtimeRequestSuccessMessage");
        }
        // 申請できなかったときの入力内容（フォームに戻して、入力し直さなくて済むようにする）
        if (sessionForError != null && sessionForError.getAttribute("overtimeRequestForm") instanceof Map) {
            request.setAttribute("form", sessionForError.getAttribute("overtimeRequestForm"));
            sessionForError.removeAttribute("overtimeRequestForm");
        }

        return new View("/WEB-INF/view/overtime_request_menu.jsp");
    }

    private LocalDate parseDateParam(String dateParam) {
        if (dateParam == null || dateParam.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateParam);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** 分を「HH:mm」に（0・null なら空） */
    private static String hhmm(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "";
        }
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /**
     * 前回の申請の内容を引き継ぐ（PJ名・摘要・理由を最初から入れておく）。
     * あわせて、過去に使った PJ名・理由の候補と、申請中・承認済みの日（まとめて申請のカレンダーで選べなくする）を渡す。
     */
    private static void setPreviousDetails(HttpServletRequest request, List<OvertimeRequest> myRequests) {
        OvertimeRequest last = null;
        for (OvertimeRequest r : myRequests) {
            if (r.getCreatedAt() != null && (last == null || r.getCreatedAt().compareTo(last.getCreatedAt()) > 0)) {
                last = r;
            }
        }
        if (last != null) {
            request.setAttribute("lastProjectName", last.getProjectName());
            request.setAttribute("lastCategory", last.getCategory());
            request.setAttribute("lastReason", last.getReason());
        }
        java.util.List<OvertimeRequest> recent = new java.util.ArrayList<>(myRequests);
        recent.sort((a, b) -> String.valueOf(b.getCreatedAt()).compareTo(String.valueOf(a.getCreatedAt())));
        java.util.Set<String> projects = new java.util.LinkedHashSet<>();
        java.util.Set<String> reasons = new java.util.LinkedHashSet<>();
        for (OvertimeRequest r : recent) {
            if (r.getProjectName() != null && !r.getProjectName().isBlank() && projects.size() < 15) projects.add(r.getProjectName());
            if (r.getReason() != null && !r.getReason().isBlank() && reasons.size() < 15) reasons.add(r.getReason());
        }
        request.setAttribute("recentProjectNames", projects);
        request.setAttribute("recentReasons", reasons);

        java.util.Map<String, String> applied = new java.util.TreeMap<>();
        for (OvertimeRequest r : myRequests) {
            if (r.isPending() || r.isApproved()) {
                applied.put(r.getTargetDate().toString(), r.getStatusLabel());
            }
        }
        try {
            request.setAttribute("appliedDatesJson", new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(applied).replace("</", "<\\/"));
        } catch (Exception e) {
            request.setAttribute("appliedDatesJson", "{}");
        }
    }
}
