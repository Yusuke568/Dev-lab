package com.example.leave.adapter.out.persistence;

import com.example.leave.domain.model.LeaveGrantRule;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.common.MyUtil;
import com.example.adapter.out.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 有給休暇関連の永続化アダプタ。
 */
public class PaidLeavePersistenceAdapter implements PaidLeavePort {

    private static final String INCREMENT_SQL = "sql/leave/increment_days.sql";
    private static final String DECREMENT_SQL = "sql/leave/decrement_days.sql";
    private static final String FIND_ALL_RULES_SQL = "sql/leave/find_all_rules.sql";
    private static final String UPDATE_DAYS_SQL = "sql/leave/update_days.sql";

    @Override
    public void incrementDays(int staffId) {
        executeUpdate(INCREMENT_SQL, staffId);
    }

    @Override
    public void decrementDays(int staffId) {
        executeUpdate(DECREMENT_SQL, staffId);
    }

    @Override
    public List<LeaveGrantRule> findAllRules() {
        List<LeaveGrantRule> rules = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(FIND_ALL_RULES_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rules.add(new LeaveGrantRule(
                            rs.getInt("years_of_service"),
                            rs.getInt("grant_days")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all leave grant rules", e);
        }
        return rules;
    }

    @Override
    public void updateDays(int staffId, int days) {
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(UPDATE_DAYS_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, days);
                pstmt.setInt(2, staffId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update leave days", e);
        }
    }

    private void executeUpdate(String sqlPath, int staffId) {
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(sqlPath);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, staffId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute update: " + sqlPath, e);
        }
    }
}
