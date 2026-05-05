package com.example.adapter.out.persistence;

import com.example.application.port.out.WorkTypePort;
import com.example.common.MyUtil;
import com.example.entity.WorkType;
import com.example.adapter.out.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 勤務区刁E��ータのための永続化アダプタ、E
 */
public class WorkTypePersistenceAdapter implements WorkTypePort {

    private static final String GET_ALL_ABSTRACT_SQL = "sql/kintai/get_abstract.sql";

    @Override
    public List<WorkType> findAll() {
        List<WorkType> workTypes = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(GET_ALL_ABSTRACT_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkType workType = new WorkType();
                    workType.setId(rs.getInt("ID"));
                    workType.setName(rs.getString("NAME"));
                    workType.setWork(rs.getBoolean("IS_WORK"));
                    workType.setPaid(rs.getBoolean("IS_PAID"));
                    workType.setLegalHoliday(rs.getBoolean("IS_LEGAL_HOLIDAY"));
                    workType.setPrescribedHoliday(rs.getBoolean("IS_PRESCRIBED_HOLIDAY"));
                    workTypes.add(workType);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all work types", e);
        }
        return workTypes;
    }
}
