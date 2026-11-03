package com.example.kintai.adapter.in.web;

import com.example.kintai.adapter.in.web.dto.WeeklyWorkReportDto;
import com.example.kintai.application.port.in.SaveWeeklyWorkReportsUseCase;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.shared.web.Action;
import com.example.shared.web.JsonApiSupport;
import com.example.shared.web.View;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 週次作業報告（木～翌水の週単位）を保存するAPIアクション。
 */
public class WeeklyWorkReportSaveAction implements Action {

    private final SaveWeeklyWorkReportsUseCase saveWeeklyWorkReportsUseCase;
    private final com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase;

    public WeeklyWorkReportSaveAction(SaveWeeklyWorkReportsUseCase saveWeeklyWorkReportsUseCase,
                                      com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase) {
        this.saveWeeklyWorkReportsUseCase = saveWeeklyWorkReportsUseCase;
        this.attendanceAccessUseCase = attendanceAccessUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;
        if (employeeId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        if (!JsonApiSupport.isJsonRequest(request)) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "送信形式が正しくありません。");
            return null;
        }
        // 表示中の勤怠表の社員（staffId パラメータ）。本人以外なら、配下の社員かどうかを確かめる
        String requestedStaffId = request.getParameter("staffId");
        final String targetStaffId = requestedStaffId == null || requestedStaffId.isBlank() ? employeeId : requestedStaffId.trim();
        @SuppressWarnings("unchecked")
        java.util.Set<String> features = (java.util.Set<String>) session.getAttribute("features");
        if (!attendanceAccessUseCase.canAccess(employeeId, features, targetStaffId)) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_FORBIDDEN, "この社員の週次作業報告を保存する権限がありません。");
            return null;
        }

        try {
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                jsonBuffer.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            List<WeeklyWorkReportDto> dtos = mapper.readValue(jsonBuffer.toString(), new TypeReference<List<WeeklyWorkReportDto>>() {});

            List<WeeklyWorkReport> reports = dtos.stream().map(dto -> new WeeklyWorkReport(
                    targetStaffId,
                    LocalDate.parse(dto.getWeekEndDate()).minusDays(6),
                    LocalDate.parse(dto.getWeekEndDate()),
                    dto.getClientName(),
                    dto.getProjectName(),
                    dto.getProcessName(),
                    dto.getProgressStatus(),
                    dto.getProgressDetail(),
                    dto.getPersonalNote(),
                    dto.getProblemCountermeasure()
            )).collect(Collectors.toList());

            saveWeeklyWorkReportsUseCase.saveWeeklyWorkReports(new EmployeeId(targetStaffId), reports);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "週次作業報告の保存に失敗しました。");
        }

        return null;
    }
}
