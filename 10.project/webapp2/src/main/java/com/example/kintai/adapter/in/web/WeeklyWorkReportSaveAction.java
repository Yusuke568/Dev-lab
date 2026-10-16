package com.example.kintai.adapter.in.web;

import com.example.kintai.adapter.in.web.dto.WeeklyWorkReportDto;
import com.example.kintai.application.port.in.SaveWeeklyWorkReportsUseCase;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.shared.web.Action;
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

    public WeeklyWorkReportSaveAction(SaveWeeklyWorkReportsUseCase saveWeeklyWorkReportsUseCase) {
        this.saveWeeklyWorkReportsUseCase = saveWeeklyWorkReportsUseCase;
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

        try {
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                jsonBuffer.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            List<WeeklyWorkReportDto> dtos = mapper.readValue(jsonBuffer.toString(), new TypeReference<List<WeeklyWorkReportDto>>() {});

            List<WeeklyWorkReport> reports = dtos.stream().map(dto -> new WeeklyWorkReport(
                    employeeId,
                    LocalDate.parse(dto.getWeekEndDate()).minusDays(6),
                    LocalDate.parse(dto.getWeekEndDate()),
                    dto.getClientName(),
                    dto.getProjectName(),
                    dto.getProcessName(),
                    dto.getProgressStatus(),
                    dto.getProgressDetail(),
                    dto.getPersonalNote()
            )).collect(Collectors.toList());

            saveWeeklyWorkReportsUseCase.saveWeeklyWorkReports(new EmployeeId(employeeId), reports);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "週次作業報告の保存に失敗しました。");
        }

        return null;
    }
}
