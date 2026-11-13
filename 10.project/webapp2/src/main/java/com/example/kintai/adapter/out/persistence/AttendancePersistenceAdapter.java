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

public class AttendancePersistenceAdapter implements LoadAttendanceRecordPort, SaveAttendanceRecordPort, LoadPendingApprovalsPort,
        com.example.kintai.domain.port.out.SubordinatePort, com.example.kintai.domain.port.out.LoadAttendancePeriodPort,
        com.example.kintai.domain.port.out.CompanyHolidayPort, com.example.kintai.domain.port.out.ReportDepartmentNamePort {

    @Override
    public String findReportDepartmentName(com.example.kintai.domain.model.employee.EmployeeId employeeId) {
        String sql = "SELECT COALESCE(NULLIF(TRIM(d.report_name), ''), d.name) AS name "
                + "FROM staff_table s JOIN department d ON d.id = s.department_id WHERE s.id = ?";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find report department name.", e);
        }
    }


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

    @Override
    public boolean hasAnyRecord(EmployeeId employeeId, YearMonth yearMonth) {
        String sql = "SELECT EXISTS (SELECT 1 FROM work_month_table WHERE staff_id = ? AND work_date >= ? AND work_date < ?)";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(yearMonth.atDay(1)));
            ps.setDate(3, java.sql.Date.valueOf(yearMonth.plusMonths(1).atDay(1)));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check monthly attendance.", e);
        }
    }

    private void autoInsertMissingDays(EmployeeId employeeId, YearMonth yearMonth, List<AttendanceRecord> existingRecords) {
        int daysInMonth = yearMonth.lengthOfMonth();
        List<LocalDate> existingDates = existingRecords.stream()
                .map(AttendanceRecord::getWorkDate)
                .collect(Collectors.toList());

        String insertSql = "INSERT INTO work_month_table(STAFF_ID, WORK_DATE, WORK_WEEK, ABSTRACT_ID, OVERTIME, APPROVAL_STATUS) VALUES(?, ?, ?, ?, 0, 0)";
        java.util.Map<LocalDate, String> companyHolidays = findCompanyHolidays(yearMonth.atDay(1), yearMonth.atEndOfMonth());
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            
            boolean hasMissing = false;
            for (int i = 1; i <= daysInMonth; i++) {
                LocalDate date = yearMonth.atDay(i);
                if (!existingDates.contains(date)) {
                    ps.setInt(1, Integer.parseInt(employeeId.getValue()));
                    ps.setDate(2, java.sql.Date.valueOf(date));
                    ps.setString(3, date.getDayOfWeek().name());
                    
                    // 土日・祝日・会社休日 -> 休日(10)、今日までの平日 -> 出勤(1)、未到来の平日 -> 未設定(NULL)
                    boolean weekend = date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY
                            || com.example.kintai.domain.service.JapaneseHolidayCalendar.isHoliday(date)
                            || companyHolidays.containsKey(date);
                    if (weekend) {
                        ps.setInt(4, 10);
                    } else if (date.isAfter(LocalDate.now())) {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(4, 1);
                    }
                    
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
    public List<AttendanceRecord> loadByEmployeeAndPeriod(EmployeeId employeeId, LocalDate from, LocalDate to) {
        List<AttendanceRecord> records = new java.util.ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(readSqlFile("get_staff_period.sql"))) {
            ps.setInt(1, Integer.parseInt(employeeId.getValue()));
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapToAttendanceRecord(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load attendance for period.", e);
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

        int staffId = Integer.parseInt(record.getEmployeeId().getValue());
        java.sql.Date workDate = java.sql.Date.valueOf(record.getWorkDate());
        // 出勤だけ打刻した日も、出勤時刻を残す
        Timestamp fromTime = record.getStartTime() != null
                ? Timestamp.valueOf(record.getWorkDate().atTime(record.getStartTime()))
                : null;
        // 日をまたいだ退勤（出勤 9:00・退勤 翌1:00 など）は、翌日の日付で記録する
        boolean endsNextDay = record.getWorkTime() != null && record.getWorkTime().isEndsNextDay();
        Timestamp toTime = record.getEndTime() != null
                ? Timestamp.valueOf(record.getWorkDate().plusDays(endsNextDay ? 1 : 0).atTime(record.getEndTime()))
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
        if (workTime == null) {
            record.setPartialTimes(startTime, endTime);
        }
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
    public List<PendingApprovalItem> loadManagedBy(String approverId) {
        String sql = readSqlFile("get_pending_approvals.sql");
        List<PendingApprovalItem> items = new ArrayList<>();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(approverId));
            ps.setInt(2, Integer.parseInt(approverId));
            ResultSet rs = ps.executeQuery();

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

    @Override
    public boolean isManagedBy(String employeeId, String approverId) {
        String sql = "WITH RECURSIVE managed AS (" +
                "SELECT id FROM department WHERE approver_staff_id = ? " +
                "UNION SELECT d.id FROM department d JOIN managed m ON d.parent_id = m.id) " +
                "SELECT 1 FROM staff_table WHERE id = ? AND id <> ? AND department_id IN (SELECT id FROM managed)";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(approverId));
            ps.setInt(2, Integer.parseInt(employeeId));
            ps.setInt(3, Integer.parseInt(approverId));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check approver of employee: " + employeeId, e);
        }
    }

    @Override
    public List<com.example.kintai.domain.model.attendance.Subordinate> findManagedStaff(String approverId) {
        List<com.example.kintai.domain.model.attendance.Subordinate> result = new java.util.ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(readSqlFile("find_managed_staff.sql"))) {
            ps.setInt(1, Integer.parseInt(approverId));
            ps.setInt(2, Integer.parseInt(approverId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new com.example.kintai.domain.model.attendance.Subordinate(
                            String.valueOf(rs.getInt("staff_id")), rs.getString("staff_name"), rs.getString("department_name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find managed staff of: " + approverId, e);
        }
        return result;
    }

    @Override
    public java.util.Optional<com.example.kintai.domain.model.attendance.Subordinate> findStaff(String staffId) {
        String sql = "SELECT s.id, s.name, d.name AS department_name FROM staff_table s "
                + "LEFT JOIN department d ON d.id = s.department_id WHERE s.id = ?";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(staffId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(new com.example.kintai.domain.model.attendance.Subordinate(
                            String.valueOf(rs.getInt("id")), rs.getString("name"), rs.getString("department_name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find staff: " + staffId, e);
        }
        return java.util.Optional.empty();
    }

    // ---- 部下のサマリー用（読むだけ） ----

    @Override
    public java.util.Map<String, List<com.example.kintai.domain.service.StaffWorkSummaryCalculator.DayRow>> findAttendanceRows(
            List<String> staffIds, LocalDate from, LocalDate to) {
        java.util.Map<String, List<com.example.kintai.domain.service.StaffWorkSummaryCalculator.DayRow>> result = new java.util.HashMap<>();
        if (staffIds.isEmpty()) return result;
        String sql = "SELECT staff_id, work_date, (job_from_time IS NOT NULL AND job_to_time IS NOT NULL) AS worked, "
                + "COALESCE(total_work_time, 0) AS total_work_time, COALESCE(overtime, 0) AS overtime, abstract_id "
                + "FROM work_month_table WHERE staff_id = ANY(?) AND work_date BETWEEN ? AND ?";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setArray(1, con.createArrayOf("integer", staffIds.stream().map(Integer::valueOf).toArray()));
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int abstractId = rs.getInt("abstract_id");
                    Integer abs = rs.wasNull() ? null : abstractId;
                    result.computeIfAbsent(String.valueOf(rs.getInt("staff_id")), k -> new java.util.ArrayList<>())
                            .add(new com.example.kintai.domain.service.StaffWorkSummaryCalculator.DayRow(
                                    rs.getDate("work_date").toLocalDate(), rs.getBoolean("worked"),
                                    rs.getInt("total_work_time"), rs.getInt("overtime"), abs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load attendance for summary.", e);
        }
        return result;
    }

    @Override
    public java.util.Map<String, List<com.example.kintai.domain.service.StaffWorkSummaryCalculator.RequestRow>> findOvertimeRequestRows(
            List<String> staffIds, LocalDate from, LocalDate to) {
        java.util.Map<String, List<com.example.kintai.domain.service.StaffWorkSummaryCalculator.RequestRow>> result = new java.util.HashMap<>();
        if (staffIds.isEmpty()) return result;
        String sql = "SELECT staff_id, target_date, status, planned_overtime_minutes FROM overtime_request "
                + "WHERE staff_id = ANY(?) AND target_date BETWEEN ? AND ?";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setArray(1, con.createArrayOf("integer", staffIds.stream().map(Integer::valueOf).toArray()));
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.computeIfAbsent(String.valueOf(rs.getInt("staff_id")), k -> new java.util.ArrayList<>())
                            .add(new com.example.kintai.domain.service.StaffWorkSummaryCalculator.RequestRow(
                                    rs.getDate("target_date").toLocalDate(), rs.getString("status"),
                                    rs.getInt("planned_overtime_minutes")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load overtime requests for summary.", e);
        }
        return result;
    }

    @Override
    public java.util.Map<String, Double> findPaidLeaveRemaining(List<String> staffIds) {
        java.util.Map<String, Double> result = new java.util.HashMap<>();
        if (staffIds.isEmpty()) return result;
        String sql = "SELECT id, paid_leave_days FROM staff_table WHERE id = ANY(?)";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setArray(1, con.createArrayOf("integer", staffIds.stream().map(Integer::valueOf).toArray()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double days = rs.getDouble("paid_leave_days");
                    if (!rs.wasNull()) result.put(String.valueOf(rs.getInt("id")), days);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load paid leave for summary.", e);
        }
        return result;
    }

    @Override
    public List<com.example.kintai.domain.model.attendance.Subordinate> findStaffInMyDepartments(String staffId) {
        List<com.example.kintai.domain.model.attendance.Subordinate> result = new java.util.ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(readSqlFile("find_staff_in_my_departments.sql"))) {
            ps.setInt(1, Integer.parseInt(staffId));
            ps.setInt(2, Integer.parseInt(staffId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new com.example.kintai.domain.model.attendance.Subordinate(
                            String.valueOf(rs.getInt("staff_id")), rs.getString("staff_name"), rs.getString("department_name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find staff in departments of: " + staffId, e);
        }
        return result;
    }

    @Override
    public java.util.Map<LocalDate, String> findCompanyHolidays(LocalDate from, LocalDate to) {
        java.util.Map<LocalDate, String> holidays = new java.util.TreeMap<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(readSqlFile("find_company_holidays.sql"))) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    holidays.put(rs.getDate("holiday_date").toLocalDate(), rs.getString("name"));
                }
            }
        } catch (Exception e) {
            // 会社休日のテーブルがまだない（マイグレーション前）などでも、勤怠画面は開けるようにする
            System.err.println("Warning: Failed to load company holidays. " + e.getMessage());
        }
        return holidays;
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
