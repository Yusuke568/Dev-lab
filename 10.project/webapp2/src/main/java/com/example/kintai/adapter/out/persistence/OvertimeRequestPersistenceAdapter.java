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
    public Optional<OvertimeRequest> findById(int requestId) {
        String sql = "SELECT id, staff_id, target_date, planned_overtime_minutes, planned_start_time, planned_end_time, reason, " +
                "status, decided_by, decided_at, created_at, current_level, project_name, category, correction_minutes, correction_mid_minutes, actual_confirmed_on, base_levels FROM overtime_request WHERE id = ?";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find overtime request: " + requestId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<OvertimeRequest> findPendingForApprover(String approverId) {
        String sql = readSqlFile("find_pending_overtime_requests.sql");
        List<OvertimeRequest> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(approverId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, rs.getString("staff_name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find pending overtime requests.", e);
        }
        return list;
    }

    @Override
    public List<OvertimeRequest> findApprovedInProgressBy(String approverId) {
        String sql = readSqlFile("find_approved_in_progress_overtime_requests.sql");
        List<OvertimeRequest> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int approver = Integer.parseInt(approverId);
            ps.setInt(1, approver);
            ps.setInt(2, approver);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, rs.getString("staff_name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find approved in-progress overtime requests.", e);
        }
        return list;
    }

    @Override
    public void advanceLevel(int requestId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE overtime_request SET current_level = current_level + 1 WHERE id = ?")) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to advance overtime request: " + requestId, e);
        }
    }

    @Override
    public int insert(EmployeeId employeeId, LocalDate date, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason) {
        String sql = readSqlFile("insert_overtime_request.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setInt(3, plannedOvertimeMinutes);
            setNullableString(ps, 4, plannedStartTime);
            setNullableString(ps, 5, plannedEndTime);
            ps.setString(6, reason);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert overtime request.", e);
        }
    }

    @Override
    public void updateDepartment(int requestId, Integer departmentId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE overtime_request SET department_id = ? WHERE id = ?")) {
            if (departmentId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, departmentId);
            }
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set department of overtime request.", e);
        }
    }

    @Override
    public List<FormRequest> findForForm(String staffId, LocalDate from, LocalDate to) {
        String sql = readSqlFile("find_overtime_requests_for_form.sql");
        java.util.Map<Integer, OvertimeRequest> requests = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, List<com.example.kintai.domain.service.OvertimeFormCalculator.Step>> steps = new java.util.HashMap<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(staffId));
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    if (!requests.containsKey(id)) {
                        requests.put(id, mapRow(rs));
                        steps.put(id, new java.util.ArrayList<>());
                    }
                    int level = rs.getInt("step_level");
                    if (!rs.wasNull()) {
                        Timestamp decidedAt = rs.getTimestamp("step_decided_at");
                        steps.get(id).add(new com.example.kintai.domain.service.OvertimeFormCalculator.Step(level,
                                rs.getString("approver_name"),
                                "APPROVED".equals(rs.getString("step_decision")),
                                decidedAt == null ? null : decidedAt.toLocalDateTime().toLocalDate()));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find overtime requests for the form.", e);
        }
        List<FormRequest> result = new java.util.ArrayList<>();
        requests.forEach((id, r) -> result.add(new FormRequest(r, steps.get(id))));
        return result;
    }

    @Override
    public void updateBaseLevels(int requestId, int baseLevels) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE overtime_request SET base_levels = ? WHERE id = ?")) {
            ps.setInt(1, baseLevels);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set base levels of overtime request.", e);
        }
    }

    @Override
    public void updateActualConfirmedOn(int requestId, LocalDate date) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE overtime_request SET actual_confirmed_on = ? WHERE id = ?")) {
            if (date == null) {
                ps.setNull(1, Types.DATE);
            } else {
                ps.setDate(1, java.sql.Date.valueOf(date));
            }
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set actual confirmed date of overtime request.", e);
        }
    }

    @Override
    public void updateCorrection(int requestId, int correctionMinutes, int correctionMidMinutes) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE overtime_request SET correction_minutes = ?, correction_mid_minutes = ? WHERE id = ?")) {
            ps.setInt(1, correctionMinutes);
            ps.setInt(2, correctionMidMinutes);
            ps.setInt(3, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set correction of overtime request.", e);
        }
    }

    @Override
    public void updateDetails(int requestId, String projectName, String category) {
        if (projectName == null && category == null) {
            return; // 古い呼び出し（PJ名・摘要を入れない）は、入っている値を消さない
        }
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE overtime_request SET project_name = ?, category = ? WHERE id = ?")) {
            setNullableString(ps, 1, projectName);
            setNullableString(ps, 2, category);
            ps.setInt(3, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set project name and category of overtime request.", e);
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
    public boolean withdraw(int requestId) {
        // 申請中のときだけ取り下げにする（承認者が先に承認・却下していたら何もしない）
        String sql = "UPDATE overtime_request SET status = 'WITHDRAWN', decided_by = NULL, decided_at = NOW() "
                + "WHERE id = ? AND status = 'PENDING'";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException("Failed to withdraw overtime request.", e);
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

        int currentLevel = rs.getInt("current_level");

        return new OvertimeRequest(id, employeeId, employeeName, targetDate, plannedOvertimeMinutes,
                plannedStartTime, plannedEndTime, reason, status, decidedBy, decidedAt, createdAt,
                currentLevel > 0 ? currentLevel : 1)
                .withDetails(optionalString(rs, "project_name"), optionalString(rs, "category"))
                .withCorrection(optionalInt(rs, "correction_minutes"), optionalInt(rs, "correction_mid_minutes"))
                .withActual(optionalDate(rs, "actual_confirmed_on"), optionalInt(rs, "base_levels"));
    }

    /** SELECT に列がない・NULL なら null */
    private static LocalDate optionalDate(ResultSet rs, String column) {
        try {
            java.sql.Date d = rs.getDate(column);
            return d == null ? null : d.toLocalDate();
        } catch (java.sql.SQLException e) {
            return null;
        }
    }

    /** SELECT に列がない・NULL なら null */
    private static Integer optionalInt(ResultSet rs, String column) {
        try {
            int v = rs.getInt(column);
            return rs.wasNull() ? null : v;
        } catch (java.sql.SQLException e) {
            return null;
        }
    }

    /** SELECT に列がなければ null（PJ名・摘要を読まない古いクエリ用） */
    private static String optionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (java.sql.SQLException e) {
            return null;
        }
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
