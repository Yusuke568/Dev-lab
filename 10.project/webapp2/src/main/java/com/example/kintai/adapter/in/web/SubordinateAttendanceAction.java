package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.AttendanceAccessUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.YearMonth;
import java.util.Set;

/**
 * 組織図上の配下の社員を一覧し、勤怠表を開くための画面（APPROVAL 権限）。
 */
public class SubordinateAttendanceAction implements Action {

    private final AttendanceAccessUseCase attendanceAccessUseCase;

    public SubordinateAttendanceAction(AttendanceAccessUseCase attendanceAccessUseCase) {
        this.attendanceAccessUseCase = attendanceAccessUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = (String) session.getAttribute("loginUser");
        @SuppressWarnings("unchecked")
        Set<String> features = (Set<String>) session.getAttribute("features");

        YearMonth yearMonth = parseYearMonth(request.getParameter("ym"));
        request.setAttribute("subordinates", attendanceAccessUseCase.getSubordinates(loginUser, features));
        request.setAttribute("targetYear", yearMonth.getYear());
        request.setAttribute("targetMonth", yearMonth.getMonthValue());
        request.setAttribute("targetYm", yearMonth.toString());
        return new View("/WEB-INF/view/subordinate_attendance.jsp");
    }

    /** 「2026-11」形式の年月。なければ今月。 */
    private static YearMonth parseYearMonth(String value) {
        if (value != null && value.matches("\\d{4}-\\d{2}")) {
            try {
                return YearMonth.parse(value);
            } catch (RuntimeException e) {
                // 不正な月は今月として扱う
            }
        }
        return YearMonth.now();
    }
}
