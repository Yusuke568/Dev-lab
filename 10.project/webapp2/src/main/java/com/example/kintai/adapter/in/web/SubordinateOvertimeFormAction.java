package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.AttendanceAccessUseCase;
import com.example.kintai.application.service.OvertimeFormExportService;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 部下の勤怠表から、部下1人・1か月分の個人別時間外勤務申請兼実績報告書（Excel）を出す（APPROVAL 権限）。
 * 出せるのは、部下の勤怠表に出る社員（組織図で自分の配下）だけ。
 */
public class SubordinateOvertimeFormAction implements Action {

    private static final Logger LOG = Logger.getLogger(SubordinateOvertimeFormAction.class.getName());
    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final AttendanceAccessUseCase attendanceAccessUseCase;
    private final OvertimeFormExportService exportService;

    public SubordinateOvertimeFormAction(AttendanceAccessUseCase attendanceAccessUseCase, OvertimeFormExportService exportService) {
        this.attendanceAccessUseCase = attendanceAccessUseCase;
        this.exportService = exportService;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            return new View("/login.do", true);
        }
        @SuppressWarnings("unchecked")
        Set<String> features = (Set<String>) session.getAttribute("features");
        String staffId = request.getParameter("id");
        YearMonth month;
        try {
            month = YearMonth.of(Integer.parseInt(request.getParameter("year")), Integer.parseInt(request.getParameter("month")));
        } catch (RuntimeException e) {
            month = YearMonth.now();
        }
        String back = "/subordinateAttendance.do?ym=" + month;
        try {
            Subordinate staff = attendanceAccessUseCase.getSubordinates(loginUser, features).stream()
                    .filter(s -> s.getStaffId().equals(staffId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("この社員の時間外申請書は出せません（部下ではありません）。"));
            byte[] bytes = exportService.export(staff, month);

            String fileName = String.format("個人別時間外勤務申請書_009版_%02d%02d_%s.xlsx",
                    month.getYear() % 100, month.getMonthValue(), staff.getName().replaceAll("[\\s　]", ""));
            String ascii = String.format("overtime_request_%d%02d_%s.xlsx", month.getYear(), month.getMonthValue(), staff.getStaffId());
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType(CONTENT_TYPE);
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            return null;
        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("subordinateMessage", e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "時間外申請書を出せませんでした（社員ID " + staffId + "、" + month + "）", e);
            session.setAttribute("subordinateMessage", "時間外申請書を出せませんでした: " + e.getMessage());
        }
        return new View(back, true);
    }
}
