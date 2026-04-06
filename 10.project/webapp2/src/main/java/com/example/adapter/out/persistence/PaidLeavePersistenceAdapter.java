package com.example.adapter.out.persistence;

import com.example.application.port.out.PaidLeavePort;
import com.example.common.MyUtil;
import com.example.infra.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PaidLeavePersistenceAdapter implements PaidLeavePort {

    private static final String INCREMENT_SQL = "sql/leave/increment_days.sql";
    private static final String DECREMENT_SQL = "sql/leave/decrement_days.sql";

    public PaidLeavePersistenceAdapter() {
    }

    @Override
    public void incrementDays(int staffId) {
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(INCREMENT_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, staffId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to increment leave days", e);
        }
    }

    @Override
    public void decrementDays(int staffId) {
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(DECREMENT_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, staffId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrement leave days", e);
        }
    }
}
