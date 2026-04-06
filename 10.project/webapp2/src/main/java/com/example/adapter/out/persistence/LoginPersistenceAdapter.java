package com.example.adapter.out.persistence;

import com.example.application.port.out.LoginPort;
import com.example.infra.ConnectionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPersistenceAdapter implements LoginPort {

    private static final String SELECT_LOGIN_SQL = 
            "SELECT USERNAME FROM login_table WHERE USERNAME = ? AND PASSWORD = ?";

    @Override
    public boolean verifyCredentials(String username, String password) {
        try (Connection conn = ConnectionBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_LOGIN_SQL)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // レコードが存在すれば認証成功
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
