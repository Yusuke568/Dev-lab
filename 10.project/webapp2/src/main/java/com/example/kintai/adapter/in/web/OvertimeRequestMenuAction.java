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

    public OvertimeRequestMenuAction(GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase,
                                      GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase) {
        this.getMyOvertimeRequestsUseCase = getMyOvertimeRequestsUseCase;
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
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
            }
        }

        HttpSession sessionForError = request.getSession(false);
        if (sessionForError != null && sessionForError.getAttribute("overtimeRequestErrorMessage") != null) {
            request.setAttribute("errorMessage", sessionForError.getAttribute("overtimeRequestErrorMessage"));
            sessionForError.removeAttribute("overtimeRequestErrorMessage");
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
}
