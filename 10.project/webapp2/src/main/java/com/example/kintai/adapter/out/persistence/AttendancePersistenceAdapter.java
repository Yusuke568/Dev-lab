package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.SaveAttendanceRecordPort;
import com.example.infra.ConnectionBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AttendancePersistenceAdapter implements LoadAttendanceRecordPort, SaveAttendanceRecordPort {
    
    private static final String SQL_BASE_PATH = "/sql/kintai/";

    @Override
    public List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId employeeId, YearMonth yearMonth) {
        String sql = readSqlFile("get_staff_month.sql");
        List<AttendanceRecord> records = new ArrayList<>();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(yearMonth.atDay(1)));
            ps.setDate(3, java.sql.Date.valueOf(yearMonth.plusMonths(1).atDay(1)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapToAttendanceRecord(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load monthly attendance.", e);
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        if (records.size() < daysInMonth) {
            autoInsertMissingDays(employeeId, yearMonth, records);
            records.clear();
            try (Connection con = ConnectionBase.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, Integer.parseInt(employeeId.getValue()));
                ps.setDate(2, java.sql.Date.valueOf(yearMonth.atDay(1)));
                ps.setDate(3, java.sql.Date.valueOf(yearMonth.plusMonths(1).atDay(1)));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        records.add(mapToAttendanceRecord(rs));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to reload monthly attendance after auto-insert.", e);
            }
        }

        return records;
    }

    private void autoInsertMissingDays(EmployeeId employeeId, YearMonth yearMonth, List<AttendanceRecord> existingRecords) {
        int daysInMonth = yearMonth.lengthOfMonth();
        List<LocalDate> existingDates = existingRecords.stream()
                .map(AttendanceRecord::getWorkDate)
                .collect(Collectors.toList());

        String insertSql = "INSERT INTO work_month_table(STAFF_ID, WORK_DATE, WORK_WEEK, ABSTRACT_ID, OVERTIME, APPROVAL_STATUS) VALUES(?, ?, ?, ?, 0, 0)";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            
            boolean hasMissing = false;
            for (int i = 1; i <= daysInMonth; i++) {
                LocalDate date = yearMonth.atDay(i);
                if (!existingDates.contains(date)) {
                    ps.setInt(1, Integer.parseInt(employeeId.getValue()));
                    ps.setDate(2, java.sql.Date.valueOf(date));
                    ps.setString(3, date.getDayOfWeek().name());
                    
                    // Weekend -> 10, Weekday -> 1
                    int abstractId = (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) ? 10 : 1;
                    ps.setInt(4, abstractId);
                    
                    ps.addBatch();
                    hasMissing = true;
                }
            }
            if (hasMissing) {
                ps.executeBatch();
            }
        } catch (Exception e) {
            System.err.println("Warning: Auto-insert failed for missing days. " + e.getMessage());
        }
    }

    @Override
    public Optional<AttendanceRecord> loadByEmployeeAndDate(EmployeeId employeeId, LocalDate date) {
        String sql = readSqlFile("get_staff_day.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToAttendanceRecord(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load daily attendance.", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(AttendanceRecord record) {
        boolean exists = loadByEmployeeAndDate(record.getEmployeeId(), record.getWorkDate()).isPresent();
        
        String sqlFileName = exists ? "update_staff_work.sql" : "insert_staff_work.sql";
        String sql = readSqlFile(sqlFileName);

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (exists) { // UPDATE
                ps.setTime(1, Time.valueOf(record.getWorkTime().getStartTime()));
                ps.setTime(2, Time.valueOf(record.getWorkTime().getEndTime()));
                // ... 他の更新項目
                ps.setInt(3, Integer.parseInt(record.getEmployeeId().getValue()));
                ps.setTimestamp(4, Timestamp.valueOf(record.getWorkDate().atStartOfDay()));
            } else { // INSERT
                ps.setInt(1, Integer.parseInt(record.getEmployeeId().getValue()));
                ps.setTimestamp(2, Timestamp.valueOf(record.getWorkDate().atStartOfDay()));
                ps.setTime(3, Time.valueOf(record.getWorkTime().getStartTime()));
                ps.setTime(4, Time.valueOf(record.getWorkTime().getEndTime()));
                // ... 他の登録項目
            }
            
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save attendance record.", e);
        }
    }

    private AttendanceRecord mapToAttendanceRecord(ResultSet rs) throws SQLException {
        EmployeeId employeeId = new EmployeeId(String.valueOf(rs.getInt("staff_id")));
        // Note: rs.getTimestamp("work_date") is used since column is WORK_DATE
        LocalDate workDate = rs.getDate("work_date").toLocalDate();
        
        LocalTime startTime = null;
        if (rs.getTimestamp("job_from_time") != null) {
            startTime = rs.getTimestamp("job_from_time").toLocalDateTime().toLocalTime();
        }
        LocalTime endTime = null;
        if (rs.getTimestamp("job_to_time") != null) {
            endTime = rs.getTimestamp("job_to_time").toLocalDateTime().toLocalTime();
        }
        WorkTime workTime = (startTime != null && endTime != null) ? new WorkTime(startTime, endTime) : null;
        
        AttendanceRecord record = new AttendanceRecord(employeeId, workDate, workTime);
        record.setWorkDescription(rs.getString("remarks"));
        
        int abstractId = rs.getInt("abstract_id");
        if (!rs.wasNull()) {
            record.setAbstractId(abstractId);
        }
        int correctionId = rs.getInt("correction_id");
        if (!rs.wasNull()) {
            record.setCorrectionId(correctionId);
        }
        int correctionUsTime = rs.getInt("correction_us_time");
        if (!rs.wasNull()) {
            record.setCorrectionUsTime(correctionUsTime);
        }
        int correctionMidTime = rs.getInt("correction_mid_time");
        if (!rs.wasNull()) {
            record.setCorrectionMidTime(correctionMidTime);
        }
        
        try {
            int approvalStatus = rs.getInt("approval_status");
            if (!rs.wasNull()) {
                record.setApprovalStatus(approvalStatus);
            }
        } catch (SQLException e) {
            // ignore if column doesn't exist
        }
        
        return record;
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
