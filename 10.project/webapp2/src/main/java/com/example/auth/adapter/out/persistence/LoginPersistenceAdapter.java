package com.example.auth.adapter.out.persistence;

import com.example.auth.domain.model.LoginAccount;
import com.example.auth.domain.port.out.LoginPort;
import com.example.common.MyUtil;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * ログイン認証のための永続化アダプタ。
 */
public class LoginPersistenceAdapter implements LoginPort {

    private static final String SELECT_LOGIN_SQL = "sql/shainlist/select_login.sql";
    private static final String SELECT_LOGIN_ACCOUNT_SQL = "sql/shainlist/select_login_account.sql";
    private static final String EXISTS_LOGIN_SQL = "sql/shainlist/exists_login.sql";
    private static final String INSERT_LOGIN_SQL = "sql/shainlist/insert_login.sql";
    private static final String UPDATE_LOGIN_PASSWORD_SQL = "sql/shainlist/update_login_password.sql";

    @Override
    public Optional<String> findPasswordHash(String username) {
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(SELECT_LOGIN_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString("PASSWORD"));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find login user", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LoginAccount> findAccount(String username) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement pstmt = con.prepareStatement(MyUtil.loadSqlFromClasspath(SELECT_LOGIN_ACCOUNT_SQL))) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LoginAccount(username, rs.getString("PASSWORD"),
                            rs.getInt("ENABLED") == 1, rs.getBoolean("MUST_CHANGE_PASSWORD")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find login account", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(String username) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement pstmt = con.prepareStatement(MyUtil.loadSqlFromClasspath(EXISTS_LOGIN_SQL))) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check login account", e);
        }
    }

    @Override
    public void insertAccount(String username, String passwordHash, boolean mustChangePassword) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement pstmt = con.prepareStatement(MyUtil.loadSqlFromClasspath(INSERT_LOGIN_SQL))) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setBoolean(3, mustChangePassword);
            pstmt.executeUpdate();
        } catch (Exception e) {
            // 例外メッセージにパスワードやハッシュを含めない
            throw new RuntimeException("Failed to create login account", e);
        }
    }

    @Override
    public void updatePassword(String username, String passwordHash, boolean mustChangePassword) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement pstmt = con.prepareStatement(MyUtil.loadSqlFromClasspath(UPDATE_LOGIN_PASSWORD_SQL))) {
            pstmt.setString(1, passwordHash);
            pstmt.setBoolean(2, mustChangePassword);
            pstmt.setString(3, username);
            if (pstmt.executeUpdate() == 0) {
                throw new IllegalStateException("Login account not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update login password", e);
        }
    }
}
