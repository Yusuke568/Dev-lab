package com.example.adapter.out.persistence;

import com.example.application.port.out.ClassmasterPort;
import com.example.common.MyUtil;
import com.example.entity.Classmaster;
import com.example.infra.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 役職データのための永続化アダプタ。
 */
public class ClassmasterPersistenceAdapter implements ClassmasterPort {

    private static final String GET_ALL_CLASS_SQL = "sql/classmaster/get_all_class.sql";

    @Override
    public List<Classmaster> findAll() {
        List<Classmaster> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(GET_ALL_CLASS_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Classmaster cm = new Classmaster();
                    cm.setId(rs.getString("ID"));
                    cm.setName(rs.getString("NAME"));
                    list.add(cm);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all classmasters", e);
        }
        return list;
    }
}
