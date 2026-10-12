package infrastructure.persistence;

import infrastructure.db.ConnectionBase;
import domain.auth.AuthRepository;
import domain.auth.UserRole;
import domain.auth.UserSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 認証・スタッフ JDBCリポジトリ実装（Driven Adapter）
 */
public class JdbcAuthRepository implements AuthRepository {

    private final DataSource dataSource;

    public JdbcAuthRepository() {
        this.dataSource = null;
    }

    public JdbcAuthRepository(DataSource dataSource) {
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
    public Optional<UserSession> authenticate(String username, String password) {
        String sql = "SELECT s.ID, s.NAME, s.CLASS_ID " +
                "FROM login_table l " +
                "JOIN staff_table s ON l.USERNAME = s.ID " +
                "WHERE l.USERNAME = ? AND l.PASSWORD = ? AND l.ENABLED = 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = String.valueOf(rs.getInt("ID"));
                    String name = rs.getString("NAME");
                    String classId = rs.getString("CLASS_ID");
                    UserRole role = UserRole.fromClassId(classId);
                    return Optional.of(new UserSession(id, name, role));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ユーザー認証処理に失敗しました: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public List<UserSession> findAllStaff() {
        String sql = "SELECT ID, NAME, CLASS_ID FROM staff_table ORDER BY ID ASC";
        List<UserSession> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("ID"));
                String name = rs.getString("NAME");
                String classId = rs.getString("CLASS_ID");
                list.add(new UserSession(id, name, UserRole.fromClassId(classId)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("社員一覧の取得に失敗しました", e);
        }
        return list;
    }
}
