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
    /** 部下ごとの当月・年間のサマリー。null なら出さない */
    private com.example.kintai.application.service.SubordinateSummaryService summaryService;

    public SubordinateAttendanceAction withSummary(com.example.kintai.application.service.SubordinateSummaryService service) {
        this.summaryService = service;
        return this;
    }

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
        java.util.List<com.example.kintai.domain.model.attendance.Subordinate> subordinates =
                attendanceAccessUseCase.getSubordinates(loginUser, features);
        request.setAttribute("subordinates", subordinates);
        if (summaryService != null) {
            try {
                java.util.Map<String, com.example.kintai.domain.service.StaffWorkSummaryCalculator.Summary> sums =
                        summaryService.summaries(subordinates, yearMonth);
                java.util.List<com.example.kintai.adapter.in.web.dto.SubordinateSummaryView> views = new java.util.ArrayList<>();
                for (com.example.kintai.domain.model.attendance.Subordinate sub : subordinates) {
                    if (sums.containsKey(sub.getStaffId())) {
                        views.add(new com.example.kintai.adapter.in.web.dto.SubordinateSummaryView(sub, sums.get(sub.getStaffId())));
                    }
                }
                request.setAttribute("summaries", views);
            } catch (RuntimeException e) {
                e.printStackTrace(); // サマリーが出せなくても勤怠表は開けるようにする
            }
        }
        request.setAttribute("monthLimitHours", com.example.kintai.domain.service.StaffWorkSummaryCalculator.MONTHLY_LIMIT_MINUTES / 60);
        request.setAttribute("yearLimitHours", com.example.kintai.domain.service.StaffWorkSummaryCalculator.YEARLY_LIMIT_MINUTES / 60);
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
