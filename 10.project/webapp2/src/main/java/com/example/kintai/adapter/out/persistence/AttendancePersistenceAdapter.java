package com.example.kintai.adapter.out.persistence;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.model.attendance.PendingApprovalItem;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.LoadPendingApprovalsPort;
import com.example.kintai.domain.port.out.SaveAttendanceRecordPort;
import com.example.shared.persistence.ConnectionBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AttendancePersistenceAdapter implements LoadAttendanceRecordPort, SaveAttendanceRecordPort, LoadPendingApprovalsPort {

    private static final java.time.format.DateTimeFormatter TIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

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
    public long sumOvertimeMinutesByEmployeeAndYear(EmployeeId employeeId, int year) {
        String sql = readSqlFile("sum_overtime_by_year.sql");
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total_overtime");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to sum annual overtime.", e);
        }
        return 0L;
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

        int staffId = Integer.parseInt(record.getEmployeeId().getValue());
        java.sql.Date workDate = java.sql.Date.valueOf(record.getWorkDate());
        Timestamp fromTime = record.getWorkTime() != null
                ? Timestamp.valueOf(record.getWorkDate().atTime(record.getWorkTime().getStartTime()))
                : null;
        Timestamp toTime = record.getWorkTime() != null
                ? Timestamp.valueOf(record.getWorkDate().atTime(record.getWorkTime().getEndTime()))
                : null;

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (exists) { // UPDATE: update_staff_work.sql の SET句 + WHERE句の順にバインド
                ps.setString(1, record.getWorkWeek());
                setIntegerOrNull(ps, 2, record.getCorrectionId());
                ps.setTimestamp(3, fromTime);
                ps.setTimestamp(4, toTime);
                setIntegerOrNull(ps, 5, record.getCorrectionUsTime());
                setIntegerOrNull(ps, 6, record.getCorrectionMidTime());
                setIntegerOrNull(ps, 7, record.getIndirectTime());
                setIntegerOrNull(ps, 8, record.getTotalWorkTime());
                setIntegerOrNull(ps, 9, record.getTotalDirectWorkTime());
                ps.setInt(10, record.getOvertimeMinutes());
                setIntegerOrNull(ps, 11, record.getAbstractId());
                ps.setString(12, record.getWorkDescription());
                ps.setInt(13, record.getApprovalStatus());
                ps.setInt(14, staffId);
                ps.setDate(15, workDate);
            } else { // INSERT: insert_staff_work.sql のカラム順にバインド
                ps.setInt(1, staffId);
                ps.setDate(2, workDate);
                ps.setString(3, record.getWorkWeek());
                setIntegerOrNull(ps, 4, record.getCorrectionId());
                ps.setTimestamp(5, fromTime);
                ps.setTimestamp(6, toTime);
                setIntegerOrNull(ps, 7, record.getCorrectionUsTime());
                setIntegerOrNull(ps, 8, record.getCorrectionMidTime());
                setIntegerOrNull(ps, 9, record.getIndirectTime());
                setIntegerOrNull(ps, 10, record.getTotalWorkTime());
                setIntegerOrNull(ps, 11, record.getTotalDirectWorkTime());
                ps.setInt(12, record.getOvertimeMinutes());
                setIntegerOrNull(ps, 13, record.getAbstractId());
                ps.setString(14, record.getWorkDescription());
                ps.setInt(15, record.getApprovalStatus());
            }

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save attendance record.", e);
        }
    }

    private void setIntegerOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
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

        record.setWorkWeek(rs.getString("work_week"));
        record.setOvertimeMinutes(rs.getInt("overtime"));

        int indirectTime = rs.getInt("indirect_time");
        if (!rs.wasNull()) {
            record.setIndirectTime(indirectTime);
        }
        int totalWorkTime = rs.getInt("total_work_time");
        if (!rs.wasNull()) {
            record.setTotalWorkTime(totalWorkTime);
        }
        int totalDirectWorkTime = rs.getInt("total_direct_work_time");
        if (!rs.wasNull()) {
            record.setTotalDirectWorkTime(totalDirectWorkTime);
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

    @Override
    public List<PendingApprovalItem> loadAll() {
        String sql = readSqlFile("get_pending_approvals.sql");
        List<PendingApprovalItem> items = new ArrayList<>();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String employeeId = String.valueOf(rs.getInt("STAFF_ID"));
                String employeeName = rs.getString("STAFF_NAME");
                String date = rs.getDate("WORK_DATE").toLocalDate().toString();
                String week = rs.getString("WORK_WEEK");

                Timestamp fromTs = rs.getTimestamp("JOB_FROM_TIME");
                Timestamp toTs = rs.getTimestamp("JOB_TO_TIME");
                String startTime = fromTs != null ? fromTs.toLocalDateTime().toLocalTime().format(TIME_FORMATTER) : "-";
                String endTime = toTs != null ? toTs.toLocalDateTime().toLocalTime().format(TIME_FORMATTER) : "-";

                String abstractName = rs.getString("ABSTRACT_NAME");
                String memo = rs.getString("REMARKS");
                int overtimeMinutes = rs.getInt("OVERTIME");

                items.add(new PendingApprovalItem(employeeId, employeeName, date, week, startTime, endTime, abstractName, memo, overtimeMinutes));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load pending approvals.", e);
        }

        return items;
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
