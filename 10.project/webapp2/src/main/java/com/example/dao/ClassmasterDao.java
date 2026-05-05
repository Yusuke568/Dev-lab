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
 * 陟厄ｽｹ髢ｨ・ｷ郢晄ｧｭ縺帷ｹｧ・ｿ郢晢ｽｼ(class_master郢昴・繝ｻ郢晄じﾎ・邵ｺ・ｸ邵ｺ・ｮ郢ｧ・｢郢ｧ・ｯ郢ｧ・ｻ郢ｧ・ｹ郢ｧ螳夲ｽ｡蠕娯鴬DAO邵ｲ繝ｻ
 */
public class ClassmasterDao {

    /**
     * 邵ｺ蜷ｶ竏狗ｸｺ・ｦ邵ｺ・ｮ陟厄ｽｹ髢ｨ・ｷ隲繝ｻ・ｰ・ｱ郢ｧ雋槫徐陟募干・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @return 陷茨ｽｨ陟厄ｽｹ髢ｨ・ｷ邵ｺ・ｮ隲繝ｻ・ｰ・ｱ邵ｺ譴ｧ・ｰ・ｼ驍城亂・・ｹｧ蠕娯螺ArrayList
     * @throws SQLException
     * @throws IOException
     */
    public List<Classmaster> findAll(Connection con) throws SQLException, IOException {

        List<Classmaster> classmasterList = new ArrayList<>();

        // 陟厄ｽｹ髢ｨ・ｷ郢ｧ雋槫徐陟募干笘・ｹｧ鬯ｼQL
        String sql = MyUtil.loadSqlFromClasspath("sql/classmaster/get_all_class.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // SQL隴√・・堤ｹｧ・ｳ郢晢ｽｳ郢ｧ・ｽ郢晢ｽｼ郢晢ｽｫ髯ｦ・ｨ驕会ｽｺ
            System.out.println(pstmt.toString());
            // SQL陞ｳ貅ｯ・｡繝ｻ
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
