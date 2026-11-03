package infrastructure.persistence;

import common.MyUtil;
import infrastructure.db.ConnectionBase;
import domain.auth.AuthRepository;
import domain.auth.PasswordHasher;
import domain.auth.UserRole;
import domain.auth.UserSession;

import javax.sql.DataSource;
import java.io.IOException;
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

    private static final String SELECT_LOGIN_SQL = "sql/shainlist/select_login.sql";
    private static final PasswordHasher PASSWORD_HASHER = new PasswordHasher();
    /** 存在しないユーザーの照合に使うハッシュ（照合時間をそろえるため） */
    private static final String DUMMY_HASH = PASSWORD_HASHER.hash("dummy-password".toCharArray());

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
        if (username == null || password == null) {
            return Optional.empty();
        }

        String storedHash = null;
        UserSession user = null;
        try (Connection con = getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(SELECT_LOGIN_SQL);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        storedHash = rs.getString("PASSWORD");
                        String id = String.valueOf(rs.getInt("ID"));
                        String name = rs.getString("NAME");
                        String classId = rs.getString("CLASS_ID");
                        user = new UserSession(id, name, UserRole.fromClassId(classId));
                    }
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("ユーザー認証処理に失敗しました: " + username, e);
        }

        // ユーザーが存在しない場合もダミーのハッシュで照合し、応答時間からユーザーの有無がわからないようにする
        boolean matched = PASSWORD_HASHER.verify(password.toCharArray(), user != null ? storedHash : DUMMY_HASH);
        return user != null && matched ? Optional.of(user) : Optional.empty();
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
