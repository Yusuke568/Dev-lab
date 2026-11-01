package com.example.auth.adapter.out.persistence;

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
}
