package com.example.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;
import com.example.entity.WorkType;

public class AbstractDao {

    /**
     * 驕ｩ逕ｨ繝槭せ繧ｿ繧貞・莉ｶ蜿門ｾ励＠縺ｾ縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @return 驕ｩ逕ｨ繝槭せ繧ｿ(WorkType)縺ｮ繝ｪ繧ｹ繝・
     * @throws SQLException
     * @throws IOException
     */
    public List<WorkType> findAll(Connection con) throws SQLException, IOException {
        List<WorkType> workTypeList = new ArrayList<>();
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_abstract.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                WorkType workType = new WorkType();
                workType.setId(rs.getInt("ID"));
                workType.setName(rs.getString("NAME"));
                workType.setWork(rs.getBoolean("IS_WORK"));
                workType.setPaid(rs.getBoolean("IS_PAID"));
                workType.setLegalHoliday(rs.getBoolean("IS_LEGAL_HOLIDAY"));
                workType.setPrescribedHoliday(rs.getBoolean("IS_PRESCRIBED_HOLIDAY"));
                workTypeList.add(workType);
            }
        }
        return workTypeList;
    }
}
