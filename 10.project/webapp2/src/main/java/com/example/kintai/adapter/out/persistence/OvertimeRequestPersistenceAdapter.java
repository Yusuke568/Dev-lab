package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.shared.persistence.ConnectionBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 残業事前申請の永続化アダプタ。
 */
public class OvertimeRequestPersistenceAdapter implements OvertimeRequestPort {

    private static final String SQL_BASE_PATH = "/sql/kintai/";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId employeeId, LocalDate date) {
        String sql = readSqlFile("find_overtime_request_by_employee_date.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find overtime request.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<OvertimeRequest> findByEmployee(EmployeeId employeeId) {
        String sql = readSqlFile("find_overtime_requests_by_employee.sql");
        List<OvertimeRequest> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find overtime requests.", e);
        }
        return list;
    }

    @Override
    public List<OvertimeRequest> findPending() {
        String sql = readSqlFile("find_pending_overtime_requests.sql");
        List<OvertimeRequest> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs, rs.getString("staff_name")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find pending overtime requests.", e);
        }
        return list;
    }

    @Override
    public void insert(EmployeeId employeeId, LocalDate date, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason) {
        String sql = readSqlFile("insert_overtime_request.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setInt(3, plannedOvertimeMinutes);
            setNullableString(ps, 4, plannedStartTime);
            setNullableString(ps, 5, plannedEndTime);
            ps.setString(6, reason);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert overtime request.", e);
        }
    }

    @Override
    public void resubmit(int requestId, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason) {
        String sql = readSqlFile("resubmit_overtime_request.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, plannedOvertimeMinutes);
            setNullableString(ps, 2, plannedStartTime);
            setNullableString(ps, 3, plannedEndTime);
            ps.setString(4, reason);
            ps.setInt(5, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to resubmit overtime request.", e);
        }
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws Exception {
        if (value != null && !value.isBlank()) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }

    @Override
    public void decide(int requestId, boolean approve, String decidedBy) {
        String sql = readSqlFile("decide_overtime_request.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, approve ? OvertimeRequest.STATUS_APPROVED : OvertimeRequest.STATUS_REJECTED);
            ps.setInt(2, Integer.parseInt(decidedBy));
            ps.setInt(3, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decide overtime request.", e);
        }
    }

    private OvertimeRequest mapRow(ResultSet rs) throws Exception {
        return mapRow(rs, null);
    }

    private OvertimeRequest mapRow(ResultSet rs, String employeeName) throws Exception {
        int id = rs.getInt("id");
        String employeeId = String.valueOf(rs.getInt("staff_id"));
        LocalDate targetDate = rs.getDate("target_date").toLocalDate();
        int plannedOvertimeMinutes = rs.getInt("planned_overtime_minutes");
        String plannedStartTime = rs.getString("planned_start_time");
        String plannedEndTime = rs.getString("planned_end_time");
        String reason = rs.getString("reason");
        String status = rs.getString("status");

        int decidedByInt = rs.getInt("decided_by");
        String decidedBy = rs.wasNull() ? null : String.valueOf(decidedByInt);

        Timestamp decidedAtTs = rs.getTimestamp("decided_at");
        String decidedAt = decidedAtTs != null ? decidedAtTs.toLocalDateTime().format(DATETIME_FORMATTER) : null;

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        String createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime().format(DATETIME_FORMATTER) : null;

        return new OvertimeRequest(id, employeeId, employeeName, targetDate, plannedOvertimeMinutes,
                plannedStartTime, plannedEndTime, reason, status, decidedBy, decidedAt, createdAt);
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
