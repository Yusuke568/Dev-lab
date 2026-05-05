package com.example.auth.adapter.out.persistence;

import com.example.auth.domain.port.out.LoginPort;
import com.example.adapter.out.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * ログイン認証のための永続化アダプタ。
 */
public class LoginPersistenceAdapter implements LoginPort {

    @Override
    public boolean authenticate(String username, String password) {
        String sql = "SELECT COUNT(*) FROM login_table WHERE USERNAME = ? AND PASSWORD = ? AND ENABLED = 1";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate user", e);
        }
        return false;
    }
}
