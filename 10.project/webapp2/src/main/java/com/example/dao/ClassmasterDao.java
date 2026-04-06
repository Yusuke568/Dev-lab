package com.example.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;

import com.example.entity.Classmaster;

/**
 * 蠖ｹ閨ｷ繝槭せ繧ｿ繝ｼ(class_master繝・・繝悶Ν)縺ｸ縺ｮ繧｢繧ｯ繧ｻ繧ｹ繧定｡後≧DAO縲・
 */
public class ClassmasterDao {

    /**
     * 縺吶∋縺ｦ縺ｮ蠖ｹ閨ｷ諠・ｱ繧貞叙蠕励＠縺ｾ縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @return 蜈ｨ蠖ｹ閨ｷ縺ｮ諠・ｱ縺梧ｼ邏阪＆繧後◆ArrayList
     * @throws SQLException
     * @throws IOException
     */
    public List<Classmaster> findAll(Connection con) throws SQLException, IOException {

        List<Classmaster> classmasterList = new ArrayList<>();

        // 蠖ｹ閨ｷ繧貞叙蠕励☆繧鬼QL
        String sql = MyUtil.loadSqlFromClasspath("sql/classmaster/get_all_class.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // SQL譁・ｒ繧ｳ繝ｳ繧ｽ繝ｼ繝ｫ陦ｨ遉ｺ
            System.out.println(pstmt.toString());
            // SQL螳溯｡・
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Classmaster classmaster = new Classmaster();
                classmaster.setId(rs.getString("ID"));
                classmaster.setName(rs.getString("NAME"));
                classmasterList.add(classmaster);
            }
        }
        return classmasterList;
    }
}
