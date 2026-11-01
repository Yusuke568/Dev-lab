package com.example.approval.adapter.out.persistence;

import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 承認ステップ（approval_step）の永続化アダプタ。
 */
public class ApprovalStepPersistenceAdapter implements ApprovalStepPort {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String DELETE_SQL =
            "DELETE FROM approval_step WHERE request_type = ? AND request_id = ?";
    private static final String INSERT_SQL =
            "INSERT INTO approval_step (request_type, request_id, level, approver_staff_id) VALUES (?, ?, ?, ?)";
    private static final String SELECT_SQL =
            "SELECT a.request_id, a.level, a.approver_staff_id, s.name AS approver_name, a.decision, a.decided_at " +
            "FROM approval_step a LEFT JOIN staff_table s ON a.approver_staff_id = s.id " +
            "WHERE a.request_type = ? AND a.request_id = ANY (?) ORDER BY a.request_id, a.level";
    private static final String DECIDE_SQL =
            "UPDATE approval_step SET decision = ?, decided_at = CURRENT_TIMESTAMP " +
            "WHERE request_type = ? AND request_id = ? AND level = ?";

    @Override
    public void replaceSteps(RequestType requestType, int requestId, List<Integer> approverStaffIds) {
        try (Connection con = ConnectionBase.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
                ps.setString(1, requestType.name());
                ps.setInt(2, requestId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
                int level = 1;
                for (Integer approver : approverStaffIds) {
                    ps.setString(1, requestType.name());
                    ps.setInt(2, requestId);
                    ps.setInt(3, level++);
                    ps.setInt(4, approver);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save approval steps.", e);
        }
    }

    @Override
    public List<ApprovalStep> findSteps(RequestType requestType, int requestId) {
        List<ApprovalStep> steps = findSteps(requestType, List.of(requestId)).get(requestId);
        return steps != null ? steps : new ArrayList<>();
    }

    @Override
    public Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> requestIds) {
        Map<Integer, List<ApprovalStep>> result = new LinkedHashMap<>();
        if (requestIds.isEmpty()) {
            return result;
        }
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SQL)) {
            Array ids = con.createArrayOf("integer", requestIds.toArray());
            ps.setString(1, requestType.name());
            ps.setArray(2, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp decidedAt = rs.getTimestamp("decided_at");
                    ApprovalStep step = new ApprovalStep(
                            rs.getInt("level"),
                            rs.getInt("approver_staff_id"),
                            rs.getString("approver_name"),
                            rs.getString("decision"),
                            decidedAt != null ? decidedAt.toLocalDateTime().format(DATETIME_FORMATTER) : null);
                    result.computeIfAbsent(rs.getInt("request_id"), k -> new ArrayList<>()).add(step);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load approval steps.", e);
        }
        return result;
    }

    @Override
    public void recordDecision(RequestType requestType, int requestId, int level, String decision) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(DECIDE_SQL)) {
            ps.setString(1, decision);
            ps.setString(2, requestType.name());
            ps.setInt(3, requestId);
            ps.setInt(4, level);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to record approval decision.", e);
        }
    }
}
