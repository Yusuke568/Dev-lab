package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.WeeklyWorkReportPort;
import com.example.shared.persistence.ConnectionBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 週次作業報告の永続化アダプタ。
 */
public class WeeklyWorkReportPersistenceAdapter implements WeeklyWorkReportPort {

    private static final String SQL_BASE_PATH = "/sql/kintai/";

    @Override
    public List<WeeklyWorkReport> findByEmployeeAndWeekEndDates(EmployeeId employeeId, List<LocalDate> weekEndDates) {
        List<WeeklyWorkReport> results = new ArrayList<>();
        for (LocalDate weekEndDate : weekEndDates) {
            findByEmployeeAndWeekEndDate(employeeId, weekEndDate).ifPresent(results::add);
        }
        return results;
    }

    @Override
    public Optional<WeeklyWorkReport> findByEmployeeAndWeekEndDate(EmployeeId employeeId, LocalDate weekEndDate) {
        String sql = readSqlFile("find_weekly_work_report.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(weekEndDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find weekly work report.", e);
        }
        return Optional.empty();
    }

    @Override
    public void upsert(WeeklyWorkReport report) {
        String sql = readSqlFile("upsert_weekly_work_report.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(report.getEmployeeId()));
            ps.setDate(2, java.sql.Date.valueOf(report.getWeekEndDate()));
            setNullableString(ps, 3, report.getClientName());
            setNullableString(ps, 4, report.getProjectName());
            setNullableString(ps, 5, report.getProcessName());
            setNullableString(ps, 6, report.getProgressStatus());
            setNullableString(ps, 7, report.getProgressDetail());
            setNullableString(ps, 8, report.getPersonalNote());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save weekly work report.", e);
        }
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws Exception {
        if (value != null && !value.isBlank()) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }

    private WeeklyWorkReport mapRow(ResultSet rs) throws Exception {
        String employeeId = String.valueOf(rs.getInt("staff_id"));
        LocalDate weekEndDate = rs.getDate("week_end_date").toLocalDate();
        LocalDate weekStartDate = weekEndDate.minusDays(6);
        String clientName = rs.getString("client_name");
        String projectName = rs.getString("project_name");
        String processName = rs.getString("process_name");
        String progressStatus = rs.getString("progress_status");
        String progressDetail = rs.getString("progress_detail");
        String personalNote = rs.getString("personal_note");

        return new WeeklyWorkReport(employeeId, weekStartDate, weekEndDate,
                clientName, projectName, processName, progressStatus, progressDetail, personalNote);
    }

    private String readSqlFile(String fileName) {
        try (InputStream is = getClass().getResourceAsStream(SQL_BASE_PATH + fileName)) {
            if (is == null) {
                throw new IOException("SQL file not found: " + fileName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL file: " + fileName, e);
        }
    }
}
