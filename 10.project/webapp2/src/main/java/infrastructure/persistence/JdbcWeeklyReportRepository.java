package infrastructure.persistence;

import infrastructure.db.ConnectionBase;
import domain.weeklyreport.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 週報 JDBCリポジトリ実装（Driven Adapter）
 */
public class JdbcWeeklyReportRepository implements WeeklyReportRepository {

    private final DataSource dataSource;

    public JdbcWeeklyReportRepository() {
        this.dataSource = null;
    }

    public JdbcWeeklyReportRepository(DataSource dataSource) {
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
    public void save(WeeklyReport report) {
        String upsertReport = "INSERT INTO weekly_reports " +
                "(id, employee_id, year_week, task_title, task_progress, personal_reflection, progress_summary, next_week_plan, workload_score, health_score, " +
                "escalation_needed, status, submitted_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "task_title = VALUES(task_title), task_progress = VALUES(task_progress), personal_reflection = VALUES(personal_reflection), " +
                "progress_summary = VALUES(progress_summary), next_week_plan = VALUES(next_week_plan), " +
                "workload_score = VALUES(workload_score), health_score = VALUES(health_score), " +
                "escalation_needed = VALUES(escalation_needed), status = VALUES(status), submitted_at = VALUES(submitted_at)";

        String deleteComments = "DELETE FROM weekly_report_comments WHERE weekly_report_id = ?";
        String insertComment = "INSERT INTO weekly_report_comments " +
                "(id, weekly_report_id, author_id, author_role, comment, is_private_to_mgmt, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(upsertReport)) {
                    ps.setString(1, report.getId());
                    ps.setString(2, report.getEmployeeId());
                    ps.setString(3, report.getYearWeek());
                    ps.setString(4, report.getTaskTitle());
                    ps.setString(5, report.getTaskProgress());
                    ps.setString(6, report.getPersonalReflection());
                    ps.setString(7, report.getProgressSummary());
                    ps.setString(8, report.getNextWeekPlan());
                    ps.setInt(9, report.getWorkloadScore());
                    ps.setInt(10, report.getHealthScore());
                    ps.setBoolean(11, report.isEscalationNeeded());
                    ps.setString(12, report.getStatus().name());
                    ps.setTimestamp(13, report.getSubmittedAt() != null ? Timestamp.valueOf(report.getSubmittedAt()) : null);
                    ps.executeUpdate();
                }

                try (PreparedStatement psDel = con.prepareStatement(deleteComments)) {
                    psDel.setString(1, report.getId());
                    psDel.executeUpdate();
                }

                try (PreparedStatement psComm = con.prepareStatement(insertComment)) {
                    for (WeeklyReportComment comm : report.getComments()) {
                        psComm.setString(1, comm.getId());
                        psComm.setString(2, report.getId());
                        psComm.setString(3, comm.getAuthorId());
                        psComm.setString(4, comm.getAuthorRole().name());
                        psComm.setString(5, comm.getComment());
                        psComm.setBoolean(6, comm.isPrivateToMgmt());
                        psComm.setTimestamp(7, Timestamp.valueOf(comm.getCreatedAt()));
                        psComm.addBatch();
                    }
                    psComm.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("週報の保存に失敗しました", e);
        }
    }

    @Override
    public Optional<WeeklyReport> findByEmployeeAndWeek(String employeeId, String yearWeek) {
        String sql = "SELECT * FROM weekly_reports WHERE employee_id = ? AND year_week = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setString(2, yearWeek);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReport(con, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("週報の取得に失敗しました", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<WeeklyReport> findById(String id) {
        String sql = "SELECT * FROM weekly_reports WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReport(con, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("週報の取得に失敗しました", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<WeeklyReport> findLatestBefore(String employeeId, String yearWeek) {
        String sql = "SELECT * FROM weekly_reports WHERE employee_id = ? AND year_week < ? ORDER BY year_week DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setString(2, yearWeek);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReport(con, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("過去週報の取得に失敗しました", e);
        }
        return Optional.empty();
    }

    private WeeklyReport mapResultSetToReport(Connection con, ResultSet rs) throws SQLException {
        String reportId = rs.getString("id");
        List<WeeklyReportComment> comments = findCommentsByReportId(con, reportId);
        Timestamp submittedAtTs = rs.getTimestamp("submitted_at");

        String taskTitle = null;
        String taskProgress = null;
        String personalReflection = null;
        try {
            taskTitle = rs.getString("task_title");
            taskProgress = rs.getString("task_progress");
            personalReflection = rs.getString("personal_reflection");
        } catch (SQLException ignored) {}

        return new WeeklyReport(
                reportId,
                rs.getString("employee_id"),
                rs.getString("year_week"),
                taskTitle,
                taskProgress,
                personalReflection,
                rs.getString("progress_summary"),
                rs.getString("next_week_plan"),
                rs.getInt("workload_score"),
                rs.getInt("health_score"),
                rs.getBoolean("escalation_needed"),
                ReportStatus.valueOf(rs.getString("status")),
                comments,
                submittedAtTs != null ? submittedAtTs.toLocalDateTime() : null
        );
    }

    private List<WeeklyReportComment> findCommentsByReportId(Connection con, String reportId) throws SQLException {
        String sql = "SELECT * FROM weekly_report_comments WHERE weekly_report_id = ? ORDER BY created_at ASC";
        List<WeeklyReportComment> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WeeklyReportComment(
                            rs.getString("id"),
                            rs.getString("author_id"),
                            AuthorRole.valueOf(rs.getString("author_role")),
                            rs.getString("comment"),
                            rs.getBoolean("is_private_to_mgmt"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return list;
    }
}
