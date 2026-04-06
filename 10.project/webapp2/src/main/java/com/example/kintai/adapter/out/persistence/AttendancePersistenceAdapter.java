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
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapToAttendanceRecord(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load monthly attendance.", e);
        }
        return records;
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
        LocalDate workDate = rs.getTimestamp("work_day").toLocalDateTime().toLocalDate();
        
        LocalTime startTime = rs.getTime("start_time").toLocalTime();
        LocalTime endTime = rs.getTime("end_time").toLocalTime();
        WorkTime workTime = new WorkTime(startTime, endTime);
        
        return new AttendanceRecord(employeeId, workDate, workTime);
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
