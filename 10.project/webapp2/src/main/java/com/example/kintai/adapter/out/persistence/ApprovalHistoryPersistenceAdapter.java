package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.attendance.ApprovalHistoryEntry;
import com.example.kintai.domain.model.attendance.ApprovalHistoryFilter;
import com.example.kintai.domain.port.out.LoadApprovalHistoryPort;
import com.example.kintai.domain.port.out.SaveApprovalHistoryPort;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 勤怠承認の履歴（監査ログ）の永続化アダプタ。
 */
public class ApprovalHistoryPersistenceAdapter implements SaveApprovalHistoryPort, LoadApprovalHistoryPort {

    private static final String SQL_BASE_PATH = "/sql/kintai/";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void record(String employeeId, LocalDate date, boolean approved, String decidedBy) {
        String sql = readSqlFile("insert_approval_history.sql");

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId));
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setString(3, approved ? "APPROVED" : "REJECTED");
            ps.setInt(4, Integer.parseInt(decidedBy));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to record approval history.", e);
        }
    }

    @Override
    public List<ApprovalHistoryEntry> loadAll(ApprovalHistoryFilter filter) {
        StringBuilder sql = new StringBuilder(readSqlFile("get_approval_history.sql"));
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (filter.getEmployeeId() != null) {
            conditions.add("h.staff_id = ?");
            params.add(Integer.parseInt(filter.getEmployeeId()));
        }
        if (filter.getDecision() != null) {
            conditions.add("h.decision = ?");
            params.add(filter.getDecision());
        }
        if (filter.getFromDate() != null) {
            conditions.add("h.work_date >= ?");
            params.add(java.sql.Date.valueOf(filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            conditions.add("h.work_date <= ?");
            params.add(java.sql.Date.valueOf(filter.getToDate()));
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        sql.append(" ORDER BY h.decided_at DESC");

        List<ApprovalHistoryEntry> list = new ArrayList<>();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else if (p instanceof java.sql.Date) {
                    ps.setDate(i + 1, (java.sql.Date) p);
                } else {
                    ps.setString(i + 1, p.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String employeeId = String.valueOf(rs.getInt("staff_id"));
                    String employeeName = rs.getString("staff_name");
                    String date = rs.getDate("work_date").toLocalDate().toString();

                    String decisionRaw = rs.getString("decision");
                    String decision = "APPROVED".equals(decisionRaw) ? "承認" : "却下";

                    String decidedById = String.valueOf(rs.getInt("decided_by"));
                    String decidedByName = rs.getString("decided_by_name");

                    Timestamp decidedAtTs = rs.getTimestamp("decided_at");
                    String decidedAt = decidedAtTs != null ? decidedAtTs.toLocalDateTime().format(DATETIME_FORMATTER) : "-";

                    list.add(new ApprovalHistoryEntry(employeeId, employeeName, date, decision, decidedById, decidedByName, decidedAt));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load approval history.", e);
        }

        return list;
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
