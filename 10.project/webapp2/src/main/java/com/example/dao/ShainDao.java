package com.example.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;
import com.example.entity.Shain;

public class ShainDao {

    /**
     * 陷茨ｽｨ驕会ｽｾ陷ｩ・｡邵ｺ・ｮ隲繝ｻ・ｰ・ｱ郢ｧ雋槫徐陟募干・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @return 陷茨ｽｨ驕会ｽｾ陷ｩ・｡邵ｺ・ｮ隲繝ｻ・ｰ・ｱ邵ｺ譴ｧ・ｰ・ｼ驍城亂・・ｹｧ蠕娯螺ArrayList
     * @throws SQLException
     * @throws IOException
     */
    public List<Shain> findAll(Connection con) throws SQLException, IOException {

        // ArrayList邵ｺ・ｮ陋ｻ譎・ｄ陋ｹ繝ｻ
        List<Shain> shainList = new ArrayList<>();

        // 驕会ｽｾ陷ｩ・｡郢ｧ雋槫徐陟募干笘・ｹｧ鬯ｼQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql");

        // SQL陞ｳ貅ｯ・｡蠕後・雋・摩・・
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // SQL隴√・・堤ｹｧ・ｳ郢晢ｽｳ郢ｧ・ｽ郢晢ｽｼ郢晢ｽｫ髯ｦ・ｨ驕会ｽｺ
            System.out.println(pstmt.toString());
            // SQL陞ｳ貅ｯ・｡繝ｻ
            ResultSet rs = pstmt.executeQuery();
            // 陷ｿ髢・ｾ蜉ｱ・邵ｺ貅ｯ・｡譴ｧ辟夂ｹｧ蝣､・ｹ・ｰ郢ｧ鬘假ｽｿ譁絶・
            while (rs.next()) {
                Shain shain = new Shain();

                // 陋滂ｽ､郢ｧ螳夲ｽｨ・ｭ陞ｳ繝ｻ
                shain.setId(rs.getInt("id"));
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setGender(rs.getString("gender"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getDouble("paid_leave_days"));
                shainList.add(shain);
            }
        }
        return shainList;
    }

    /**
     * 驕会ｽｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ郢ｧ蝣､蛹ｳ鬪ｭ・ｲ邵ｺ蜉ｱ竏ｪ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @param shain 騾具ｽｻ鬪ｭ・ｲ邵ｺ蜷ｶ・矩♂・ｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ
     * @throws SQLException
     * @throws IOException
     */
    public void create(Connection con, Shain shain) throws SQLException, IOException {
        //驕会ｽｾ陷ｩ・｡郢ｧ蝣､蛹ｳ鬪ｭ・ｲ邵ｺ蜷ｶ・鬼QL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/insert_shain.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            //郢昜ｻ｣ﾎ帷ｹ晢ｽ｡郢晢ｽｼ郢ｧ・ｿ郢ｧ諡・L邵ｺ・ｫ郢ｧ・ｻ郢昴・繝ｨ
            pstmt.setInt(1, shain.getId());
            pstmt.setString(2, shain.getName());
            pstmt.setString(3, shain.getNamekana());
            pstmt.setInt(4, shain.getEntryyear());
            pstmt.setString(5, shain.getGender());
            pstmt.setBigDecimal(6, java.math.BigDecimal.valueOf(shain.getPaidLeaveDays()));
            pstmt.setString(7, shain.getJobclass());

            //SQL隴√・・帝勗・ｨ驕会ｽｺ
            System.out.println(pstmt.toString());

            //SQL陞ｳ貅ｯ・｡繝ｻ
            pstmt.executeUpdate();
        }
    }

    /**
     * ID郢ｧ蛛ｵ縺冗ｹ晢ｽｼ邵ｺ・ｫ驕会ｽｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ郢ｧ繝ｻ闔会ｽｶ陷ｿ髢・ｾ蜉ｱ・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @param id 陷ｿ髢・ｾ蜉ｱ笘・ｹｧ迢暦ｽ､・ｾ陷ｩ・｡邵ｺ・ｮID
     * @return 陷ｿ髢・ｾ蜉ｱ・邵ｺ貅ｽ・､・ｾ陷ｩ・｡邵ｺ・ｮ隲繝ｻ・ｰ・ｱ邵ｲ繧奇ｽｦ荵昶命邵ｺ荵晢ｽ臥ｸｺ・ｪ邵ｺ繝ｻ・ｰ・ｴ陷ｷ蛹ｻ繝ｻnull邵ｲ繝ｻ
     * @throws SQLException
     * @throws IOException
     */
    public Shain findById(Connection con, int id) throws SQLException, IOException {
        Shain shain = null;

        //驕会ｽｾ陷ｩ・｡郢ｧ雋槫徐陟募干笘・ｹｧ鬯ｼQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/select_shain.sql");

        //SQL陞ｳ貅ｯ・｡蠕後・雋・摩・・
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //郢昜ｻ｣ﾎ帷ｹ晢ｽ｡郢晢ｽｼ郢ｧ・ｿ郢ｧ諡・L邵ｺ・ｫ郢ｧ・ｻ郢昴・繝ｨ
            pstmt.setInt(1, id);
            //SQL隴√・・帝勗・ｨ驕会ｽｺ
            System.out.println(pstmt.toString());
            //SQL陞ｳ貅ｯ・｡繝ｻ
            ResultSet rs = pstmt.executeQuery();
            // 陷ｿ髢・ｾ蜉ｱ・邵ｺ貅ｯ・｡蠕娯ｲ邵ｺ繧・ｽ檎ｸｺ・ｰ
            if (rs.next()) {
                shain = new Shain();
                //陋滂ｽ､郢ｧ雋槫徐陟輔・
                shain.setId(id);
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setGender(rs.getString("gender"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getDouble("paid_leave_days"));
            }
        }
        return shain;
    }

    /**
     * 驕会ｽｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ郢ｧ蜻亥ｳｩ隴・ｽｰ邵ｺ蜉ｱ竏ｪ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @param shain 隴厄ｽｴ隴・ｽｰ邵ｺ蜷ｶ・矩♂・ｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ
     * @throws SQLException
     * @throws IOException
     */
    public void update(Connection con, Shain shain) throws SQLException, IOException {
        //驕会ｽｾ陷ｩ・｡郢ｧ蜻亥ｳｩ隴・ｽｰ邵ｺ蜷ｶ・鬼QL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/update_shain.sql");
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //郢昜ｻ｣ﾎ帷ｹ晢ｽ｡郢晢ｽｼ郢ｧ・ｿ郢ｧ諡・L邵ｺ・ｫ郢ｧ・ｻ郢昴・繝ｨ
            pstmt.setString(1, shain.getName());
            pstmt.setString(2, shain.getNamekana());
            pstmt.setInt(3, shain.getEntryyear());
            pstmt.setString(4, shain.getGender());
            pstmt.setBigDecimal(5, java.math.BigDecimal.valueOf(shain.getPaidLeaveDays()));
            pstmt.setString(6, shain.getJobclass());
            pstmt.setInt(7, shain.getId());
            //SQL隴√・・帝勗・ｨ驕会ｽｺ
            System.out.println(pstmt.toString());
            //SQL陞ｳ貅ｯ・｡繝ｻ
            pstmt.executeUpdate();
        }
    }

    /**
     * ID郢ｧ蛛ｵ縺冗ｹ晢ｽｼ邵ｺ・ｫ驕会ｽｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ郢ｧ雋樒ｎ鬮ｯ・､邵ｺ蜉ｱ竏ｪ邵ｺ蜷ｶﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @param id 陷台ｼ∝求邵ｺ蜷ｶ・矩♂・ｾ陷ｩ・｡邵ｺ・ｮID
     * @throws SQLException
     * @throws IOException
     */
    public void deleteById(Connection con, int id) throws SQLException, IOException {
        //驕会ｽｾ陷ｩ・｡郢ｧ雋樒ｎ鬮ｯ・､邵ｺ蜷ｶ・鬼QL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/delete_shain.sql");
        //SQL陞ｳ貅ｯ・｡蠕後・雋・摩・・
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //郢昜ｻ｣ﾎ帷ｹ晢ｽ｡郢晢ｽｼ郢ｧ・ｿ郢ｧ諡・L邵ｺ・ｫ郢ｧ・ｻ郢昴・繝ｨ
            pstmt.setInt(1, id);
            //SQL隴√・・帝勗・ｨ驕会ｽｺ
            System.out.println(pstmt.toString());
            //SQL陞ｳ貅ｯ・｡繝ｻ
            pstmt.executeUpdate();
        }
    }

    /**
     * 陷ｷ讎顔√郢ｧ蛛ｵ縺冗ｹ晢ｽｼ邵ｺ・ｫ驕会ｽｾ陷ｩ・｡隲繝ｻ・ｰ・ｱ郢ｧ蜻茨ｽ､諛・ｽｴ・｢邵ｺ蜉ｱ竏ｪ邵ｺ蜻ｻ・ｼ逎ｯﾎ夊崕繝ｻ・ｸﾂ髢ｾ・ｴ繝ｻ蟲ｨﾂ繝ｻ
     * @param con 郢昴・繝ｻ郢ｧ・ｿ郢晏生繝ｻ郢ｧ・ｹ隰暦ｽ･驍ｯ繝ｻ
     * @param name 隶諛・ｽｴ・｢邵ｺ蜷ｶ・玖惺讎顔√
     * @return 陷ｿ髢・ｾ蜉ｱ・邵ｺ貅ｽ・､・ｾ陷ｩ・｡邵ｺ・ｮ隲繝ｻ・ｰ・ｱ郢晢ｽｪ郢ｧ・ｹ郢昴・
     * @throws SQLException
     * @throws IOException
     */
    public List<Shain> searchByName(Connection con, String name) throws SQLException, IOException {
        List<Shain> shainList = new ArrayList<>();
        // get_all_shain.sql郢ｧ蜻茨ｽｵ竏ｫ逡醍ｸｺ蜉ｱﾂ莉扎ERE陷ｿ・･郢ｧ螳夲ｽｿ・ｽ陷会｣ｰ邵ｺ蜷ｶ・・
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql") + " WHERE name LIKE ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            System.out.println(pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Shain shain = new Shain();
                shain.setId(rs.getInt("id"));
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setGender(rs.getString("gender"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getDouble("paid_leave_days"));
                shainList.add(shain);
            }
        }
        return shainList;
    }
}
