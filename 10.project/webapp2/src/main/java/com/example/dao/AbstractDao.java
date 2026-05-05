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
     * 鬩包ｽｩ騾包ｽｨ郢晄ｧｭ縺帷ｹｧ・ｿ郢ｧ雋槭・闔会ｽｶ陷ｿ髢・ｾ蜉ｱ・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @return 鬩包ｽｩ騾包ｽｨ郢晄ｧｭ縺帷ｹｧ・ｿ(WorkType)邵ｺ・ｮ郢晢ｽｪ郢ｧ・ｹ郢昴・
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
