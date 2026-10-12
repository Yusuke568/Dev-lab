package infrastructure.persistence;

import infrastructure.db.ConnectionBase;
import domain.attendance.*;
import domain.contract.SettlementStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 日次勤怠・月次サマリ JDBCリポジトリ実装（Driven Adapter）
 */
public class JdbcAttendanceRepository implements AttendanceRepository {

    private final DataSource dataSource;

    public JdbcAttendanceRepository() {
        this.dataSource = null;
    }

    public JdbcAttendanceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        try {
            return ConnectionBase.getConnection();
        } catch (Exception e) {
            throw new SQLException("データベース接続の取得に失敗しました", e);
        }
    }

    @Override
    public void save(DailyAttendance attendance) {
        String upsertDaily = "INSERT INTO daily_attendances " +
                "(id, employee_id, work_date, start_time, end_time, break_minutes, abstract_id, leave_type, remarks, is_filled, is_locked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "start_time = VALUES(start_time), end_time = VALUES(end_time), break_minutes = VALUES(break_minutes), " +
                "abstract_id = VALUES(abstract_id), leave_type = VALUES(leave_type), remarks = VALUES(remarks), is_filled = VALUES(is_filled), is_locked = VALUES(is_locked)";

        String deleteSegments = "DELETE FROM work_time_segments WHERE daily_attendance_id = ?";
        String insertSegment = "INSERT INTO work_time_segments (id, daily_attendance_id, category, duration_minutes, task_description) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. 日次勤怠の保存
                try (PreparedStatement ps = con.prepareStatement(upsertDaily)) {
                    ps.setString(1, attendance.getId());
                    ps.setString(2, attendance.getEmployeeId());
                    ps.setDate(3, java.sql.Date.valueOf(attendance.getWorkDate()));
                    ps.setTime(4, attendance.getStartTime() != null ? java.sql.Time.valueOf(attendance.getStartTime()) : null);
                    ps.setTime(5, attendance.getEndTime() != null ? java.sql.Time.valueOf(attendance.getEndTime()) : null);
                    ps.setInt(6, attendance.getBreakMinutes());
                    ps.setInt(7, attendance.getAbstractId());
                    ps.setString(8, attendance.getLeaveType().name());
                    ps.setString(9, attendance.getRemarks());
                    ps.setBoolean(10, attendance.isFilled());
                    ps.setBoolean(11, attendance.isLocked());
                    ps.executeUpdate();
                }

                // 2. セグメントの再保存
                try (PreparedStatement psDel = con.prepareStatement(deleteSegments)) {
                    psDel.setString(1, attendance.getId());
                    psDel.executeUpdate();
                }

                try (PreparedStatement psSeg = con.prepareStatement(insertSegment)) {
                    for (WorkTimeSegment seg : attendance.getSegments()) {
                        psSeg.setString(1, seg.getId());
                        psSeg.setString(2, attendance.getId());
                        psSeg.setString(3, seg.getCategory().name());
                        psSeg.setInt(4, seg.getDurationMinutes());
                        psSeg.setString(5, seg.getTaskDescription());
                        psSeg.addBatch();
                    }
                    psSeg.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("勤怠の保存に失敗しました: " + attendance.getId(), e);
        }
    }

    @Override
    public void saveAll(List<DailyAttendance> attendances) {
        for (DailyAttendance a : attendances) {
            save(a);
        }
    }

    @Override
    public Optional<DailyAttendance> findByEmployeeAndDate(String employeeId, LocalDate date) {
        String sql = "SELECT da.*, am.NAME AS abstract_name FROM daily_attendances da " +
                "LEFT JOIN abstract_master am ON da.abstract_id = am.ID " +
                "WHERE da.employee_id = ? AND da.work_date = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String attId = rs.getString("id");
                    List<WorkTimeSegment> segments = findSegmentsByAttendanceId(con, attId);
                    return Optional.of(mapResultSetToAttendance(rs, segments));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("日次勤怠の取得に失敗しました", e);
        }
        return Optional.empty();
    }

    @Override
    public List<DailyAttendance> findByEmployeeAndMonth(String employeeId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        String sql = "SELECT da.*, am.NAME AS abstract_name FROM daily_attendances da " +
                "LEFT JOIN abstract_master am ON da.abstract_id = am.ID " +
                "WHERE da.employee_id = ? AND da.work_date BETWEEN ? AND ? ORDER BY da.work_date ASC";

        List<DailyAttendance> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String attId = rs.getString("id");
                    List<WorkTimeSegment> segments = findSegmentsByAttendanceId(con, attId);
                    list.add(mapResultSetToAttendance(rs, segments));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("月間勤怠の取得に失敗しました", e);
        }
        return list;
    }

    private List<WorkTimeSegment> findSegmentsByAttendanceId(Connection con, String attId) throws SQLException {
        String sql = "SELECT * FROM work_time_segments WHERE daily_attendance_id = ?";
        List<WorkTimeSegment> segs = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, attId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    segs.add(new WorkTimeSegment(
                            rs.getString("id"),
                            WorkCategory.valueOf(rs.getString("category")),
                            rs.getInt("duration_minutes"),
                            rs.getString("task_description")
                    ));
                }
            }
        }
        return segs;
    }

    private DailyAttendance mapResultSetToAttendance(ResultSet rs, List<WorkTimeSegment> segments) throws SQLException {
        Time st = rs.getTime("start_time");
        Time et = rs.getTime("end_time");
        int abstractId = 1;
        try {
            abstractId = rs.getInt("abstract_id");
            if (abstractId <= 0) abstractId = 1;
        } catch (SQLException ignored) {}
        String abstractName = null;
        try {
            abstractName = rs.getString("abstract_name");
        } catch (SQLException ignored) {}

        DailyAttendance att = new DailyAttendance(
                rs.getString("id"),
                rs.getString("employee_id"),
                rs.getDate("work_date").toLocalDate(),
                st != null ? st.toLocalTime() : null,
                et != null ? et.toLocalTime() : null,
                rs.getInt("break_minutes"),
                abstractId,
                LeaveType.valueOf(rs.getString("leave_type")),
                rs.getString("remarks"),
                rs.getBoolean("is_filled"),
                rs.getBoolean("is_locked"),
                segments
        );
        if (abstractName != null) {
            att.setAbstractName(abstractName);
        }
        return att;
    }

    @Override
    public void saveMonthlySummary(MonthlySummary summary) {
        String sql = "INSERT INTO monthly_summaries " +
                "(id, employee_id, `year_month`, assignment_id, total_work_minutes, direct_minutes, indirect_minutes, " +
                "legal_overtime_minutes, settlement_status, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_work_minutes = VALUES(total_work_minutes), direct_minutes = VALUES(direct_minutes), " +
                "indirect_minutes = VALUES(indirect_minutes), legal_overtime_minutes = VALUES(legal_overtime_minutes), " +
                "settlement_status = VALUES(settlement_status), status = VALUES(status)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, summary.getId());
            ps.setString(2, summary.getEmployeeId());
            ps.setString(3, summary.getYearMonth());
            ps.setString(4, summary.getAssignmentId());
            ps.setInt(5, summary.getTotalWorkMinutes());
            ps.setInt(6, summary.getDirectMinutes());
            ps.setInt(7, summary.getIndirectMinutes());
            ps.setInt(8, summary.getLegalOvertimeMinutes());
            ps.setString(9, summary.getSettlementStatus().name());
            ps.setString(10, summary.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("月次サマリの保存に失敗しました", e);
        }
    }

    @Override
    public Optional<MonthlySummary> findMonthlySummary(String employeeId, YearMonth yearMonth) {
        String sql = "SELECT * FROM monthly_summaries WHERE employee_id = ? AND `year_month` = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setString(2, yearMonth.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MonthlySummary s = new MonthlySummary(
                            rs.getString("id"),
                            rs.getString("employee_id"),
                            rs.getString("year_month"),
                            rs.getString("assignment_id")
                    );
                    s.updateSummaries(
                            rs.getInt("total_work_minutes"),
                            rs.getInt("direct_minutes"),
                            rs.getInt("indirect_minutes"),
                            rs.getInt("legal_overtime_minutes"),
                            SettlementStatus.valueOf(rs.getString("settlement_status"))
                    );
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("月次サマリの取得に失敗しました", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AbstractItem> findAllAbstractItems() {
        String sql = "SELECT ID, NAME, IS_WORK, IS_PAID, IS_LEGAL_HOLIDAY, IS_PRESCRIBED_HOLIDAY FROM abstract_master ORDER BY ID ASC";
        List<AbstractItem> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AbstractItem(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getBoolean("IS_WORK"),
                        rs.getBoolean("IS_PAID"),
                        rs.getBoolean("IS_LEGAL_HOLIDAY"),
                        rs.getBoolean("IS_PRESCRIBED_HOLIDAY")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("摘要マスタの取得に失敗しました", e);
        }
        return list;
    }
}
