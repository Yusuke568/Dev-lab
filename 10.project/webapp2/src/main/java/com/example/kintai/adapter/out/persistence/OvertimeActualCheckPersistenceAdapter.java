package com.example.kintai.adapter.out.persistence;

import com.example.shared.persistence.ConnectionBase;
import com.example.kintai.domain.port.out.OvertimeActualCheckPort;
import com.example.kintai.domain.service.OvertimeFormCalculator.Step;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 残業の実績確認（overtime_actual_step と overtime_request.actual_status / actual_level）。
 */
public class OvertimeActualCheckPersistenceAdapter implements OvertimeActualCheckPort {

    @Override
    public State findState(int requestId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT actual_status, actual_level FROM overtime_request WHERE id = ?")) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return State.NONE;
                int level = rs.getInt("actual_level");
                return new State(rs.getString("actual_status"), rs.wasNull() ? null : level);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load actual check state.", e);
        }
    }

    @Override
    public void start(int requestId, List<List<Integer>> levels) {
        try (Connection con = ConnectionBase.getConnection()) {
            try (PreparedStatement del = con.prepareStatement("DELETE FROM overtime_actual_step WHERE request_id = ?")) {
                del.setInt(1, requestId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO overtime_actual_step (request_id, level, approver_staff_id) VALUES (?, ?, ?) "
                            + "ON CONFLICT (request_id, level, approver_staff_id) DO NOTHING")) {
                for (int i = 0; i < levels.size(); i++) {
                    for (Integer staffId : levels.get(i)) {
                        ins.setInt(1, requestId);
                        ins.setInt(2, i + 1);
                        ins.setInt(3, staffId);
                        ins.addBatch();
                    }
                }
                ins.executeBatch();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start actual check.", e);
        }
        updateState(requestId, PENDING, 1);
    }

    @Override
    public void reset(int requestId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement del = con.prepareStatement("DELETE FROM overtime_actual_step WHERE request_id = ?")) {
            del.setInt(1, requestId);
            del.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset actual check.", e);
        }
        updateState(requestId, null, null);
    }

    @Override
    public boolean check(int requestId, int level, int approverStaffId) {
        return check(requestId, level, approverStaffId, null);
    }

    @Override
    public boolean check(int requestId, int level, int approverStaffId, String comment) {
        String sql = "UPDATE overtime_actual_step SET checked_at = NOW(), comment = ? "
                + "WHERE request_id = ? AND level = ? AND approver_staff_id = ? AND checked_at IS NULL "
                + "AND NOT EXISTS (SELECT 1 FROM overtime_actual_step x WHERE x.request_id = ? AND x.level = ? AND x.checked_at IS NOT NULL)";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, comment);
            ps.setInt(2, requestId);
            ps.setInt(3, level);
            ps.setInt(4, approverStaffId);
            ps.setInt(5, requestId);
            ps.setInt(6, level);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException("Failed to record actual check.", e);
        }
    }

    @Override
    public void updateState(int requestId, String status, Integer level) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE overtime_request SET actual_status = ?, actual_level = ? WHERE id = ?")) {
            if (status == null) ps.setNull(1, Types.VARCHAR); else ps.setString(1, status);
            if (level == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, level);
            ps.setInt(3, requestId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update actual check state.", e);
        }
    }

    @Override
    public Map<Integer, String> findPendingForApprover(int approverStaffId) {
        String sql = "SELECT r.id, s.name FROM overtime_request r "
                + "JOIN staff_table s ON s.id = r.staff_id "
                + "JOIN overtime_actual_step a ON a.request_id = r.id AND a.level = r.actual_level "
                + "WHERE r.actual_status = 'PENDING' AND a.approver_staff_id = ? AND a.checked_at IS NULL "
                + "AND NOT EXISTS (SELECT 1 FROM overtime_actual_step x WHERE x.request_id = r.id "
                + "                AND x.level = r.actual_level AND x.checked_at IS NOT NULL) "
                + "ORDER BY r.staff_id, r.target_date";
        Map<Integer, String> result = new LinkedHashMap<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, approverStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find pending actual checks.", e);
        }
        return result;
    }

    @Override
    public Map<Integer, List<Step>> findSteps(Collection<Integer> requestIds) {
        Map<Integer, List<Step>> result = new HashMap<>();
        if (requestIds == null || requestIds.isEmpty()) return result;
        String sql = "SELECT a.request_id, a.level, a.checked_at, a.comment, s.name FROM overtime_actual_step a "
                + "LEFT JOIN staff_table s ON s.id = a.approver_staff_id "
                + "WHERE a.request_id = ANY(?) ORDER BY a.request_id, a.level, a.id";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setArray(1, con.createArrayOf("integer", requestIds.toArray()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp checked = rs.getTimestamp("checked_at");
                    result.computeIfAbsent(rs.getInt("request_id"), k -> new ArrayList<>())
                            .add(new Step(rs.getInt("level"), rs.getString("name"), checked != null,
                                    checked == null ? null : checked.toLocalDateTime().toLocalDate(),
                                    rs.getString("comment")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load actual check steps.", e);
        }
        return result;
    }
}
