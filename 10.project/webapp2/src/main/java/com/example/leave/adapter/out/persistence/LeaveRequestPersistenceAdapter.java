package com.example.leave.adapter.out.persistence;

import com.example.leave.domain.model.LeaveRequest;
import com.example.leave.domain.port.out.LeaveRequestPort;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 休暇申請（leave_request）の永続化アダプタ。
 */
public class LeaveRequestPersistenceAdapter implements LeaveRequestPort {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String SELECT_BASE =
            "SELECT r.id, r.staff_id, s.name AS staff_name, r.target_date, r.abstract_id, am.name AS abstract_name, " +
            "r.reason, r.status, r.current_level, r.created_at " +
            "FROM leave_request r " +
            "JOIN staff_table s ON r.staff_id = s.id " +
            "LEFT JOIN abstract_master am ON r.abstract_id = am.id ";

    @Override
    public Optional<LeaveRequest> findById(int requestId) {
        List<LeaveRequest> list = query(SELECT_BASE + "WHERE r.id = ?", requestId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<LeaveRequest> findByEmployeeAndDate(int staffId, LocalDate date) {
        List<LeaveRequest> list = query(SELECT_BASE + "WHERE r.staff_id = ? AND r.target_date = ?",
                staffId, java.sql.Date.valueOf(date));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<LeaveRequest> findByEmployee(int staffId) {
        return query(SELECT_BASE + "WHERE r.staff_id = ? ORDER BY r.target_date DESC", staffId);
    }

    @Override
    public List<LeaveRequest> findPendingForApprover(int approverId) {
        return query(SELECT_BASE +
                "JOIN approval_step a ON a.request_type = 'LEAVE' AND a.request_id = r.id AND a.level = r.current_level " +
                "WHERE r.status = 'PENDING' AND a.decision IS NULL AND a.approver_staff_id = ? " +
                // 同じ段のほかの候補者（副承認者など）がすでに判定していたら出さない
                "AND NOT EXISTS (SELECT 1 FROM approval_step x WHERE x.request_type = a.request_type " +
                "AND x.request_id = a.request_id AND x.level = a.level AND x.decision IS NOT NULL) " +
                "ORDER BY r.target_date ASC", approverId);
    }

    @Override
    public int insert(int staffId, LocalDate date, int abstractId, String reason) {
        String sql = "INSERT INTO leave_request (staff_id, target_date, abstract_id, reason, status, current_level, created_at) " +
                "VALUES (?, ?, ?, ?, 'PENDING', 1, CURRENT_TIMESTAMP) RETURNING id";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setInt(3, abstractId);
            ps.setString(4, reason);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert leave request.", e);
        }
    }

    @Override
    public void resubmit(int requestId, int abstractId, String reason) {
        update("UPDATE leave_request SET abstract_id = ?, reason = ?, status = 'PENDING', current_level = 1, " +
                "created_at = CURRENT_TIMESTAMP WHERE id = ?", abstractId, reason, requestId);
    }

    @Override
    public void updateDepartment(int requestId, Integer departmentId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE leave_request SET department_id = ? WHERE id = ?")) {
            if (departmentId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, departmentId);
            }
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set department of leave request.", e);
        }
    }

    @Override
    public void updateStatus(int requestId, String status) {
        update("UPDATE leave_request SET status = ? WHERE id = ?", status, requestId);
    }

    @Override
    public void advanceLevel(int requestId) {
        update("UPDATE leave_request SET current_level = current_level + 1 WHERE id = ?", requestId);
    }

    private List<LeaveRequest> query(String sql, Object... params) {
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    list.add(new LeaveRequest(
                            rs.getInt("id"),
                            String.valueOf(rs.getInt("staff_id")),
                            rs.getString("staff_name"),
                            rs.getDate("target_date").toLocalDate(),
                            rs.getInt("abstract_id"),
                            rs.getString("abstract_name"),
                            rs.getString("reason"),
                            rs.getString("status"),
                            rs.getInt("current_level"),
                            createdAt != null ? createdAt.toLocalDateTime().format(DATETIME_FORMATTER) : null));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load leave requests.", e);
        }
        return list;
    }

    private void update(String sql, Object... params) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update leave request.", e);
        }
    }
}
