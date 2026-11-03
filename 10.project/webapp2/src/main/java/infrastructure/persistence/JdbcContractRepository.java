package infrastructure.persistence;

import infrastructure.db.ConnectionBase;
import domain.contract.Assignment;
import domain.contract.ContractRepository;
import domain.contract.ProjectContract;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

/**
 * 契約・アサイン情報 JDBCリポジトリ実装（Driven Adapter）
 */
public class JdbcContractRepository implements ContractRepository {

    private final DataSource dataSource;

    public JdbcContractRepository() {
        this.dataSource = null; // ConnectionBase を使用
    }

    public JdbcContractRepository(DataSource dataSource) {
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
    public Optional<ProjectContract> findContractById(String contractId) {
        String sql = "SELECT id, client_name, billing_lower_limit_hours, billing_upper_limit_hours, " +
                     "default_start_time, default_end_time, default_break_minutes " +
                     "FROM project_contracts WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ProjectContract(
                            rs.getString("id"),
                            rs.getString("client_name"),
                            rs.getDouble("billing_lower_limit_hours"),
                            rs.getDouble("billing_upper_limit_hours"),
                            rs.getTime("default_start_time").toLocalTime(),
                            rs.getTime("default_end_time").toLocalTime(),
                            rs.getInt("default_break_minutes")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("契約情報の取得に失敗しました: " + contractId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Assignment> findActiveAssignment(String employeeId) {
        String sql = "SELECT id, employee_id, project_contract_id, start_date, end_date, allocation_ratio " +
                     "FROM assignments WHERE employee_id = ? ORDER BY start_date DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date endDate = rs.getDate("end_date");
                    return Optional.of(new Assignment(
                            rs.getString("id"),
                            rs.getString("employee_id"),
                            rs.getString("project_contract_id"),
                            rs.getDate("start_date").toLocalDate(),
                            endDate != null ? endDate.toLocalDate() : null,
                            rs.getDouble("allocation_ratio")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("アサイン情報の取得に失敗しました: " + employeeId, e);
        }
        return Optional.empty();
    }
}
