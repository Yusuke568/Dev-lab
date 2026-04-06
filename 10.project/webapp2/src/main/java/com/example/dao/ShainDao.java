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
     * 蜈ｨ遉ｾ蜩｡縺ｮ諠・ｱ繧貞叙蠕励＠縺ｾ縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @return 蜈ｨ遉ｾ蜩｡縺ｮ諠・ｱ縺梧ｼ邏阪＆繧後◆ArrayList
     * @throws SQLException
     * @throws IOException
     */
    public List<Shain> findAll(Connection con) throws SQLException, IOException {

        // ArrayList縺ｮ蛻晄悄蛹・
        List<Shain> shainList = new ArrayList<>();

        // 遉ｾ蜩｡繧貞叙蠕励☆繧鬼QL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql");

        // SQL螳溯｡後・貅門ｙ
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // SQL譁・ｒ繧ｳ繝ｳ繧ｽ繝ｼ繝ｫ陦ｨ遉ｺ
            System.out.println(pstmt.toString());
            // SQL螳溯｡・
            ResultSet rs = pstmt.executeQuery();
            // 蜿門ｾ励＠縺溯｡梧焚繧堤ｹｰ繧願ｿ斐☆
            while (rs.next()) {
                Shain shain = new Shain();

                // 蛟､繧定ｨｭ螳・
                shain.setId(rs.getInt("id"));
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setGender(rs.getString("gender"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getInt("paid_leave_days"));
                shainList.add(shain);
            }
        }
        return shainList;
    }

    /**
     * 遉ｾ蜩｡諠・ｱ繧堤匳骭ｲ縺励∪縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @param shain 逋ｻ骭ｲ縺吶ｋ遉ｾ蜩｡諠・ｱ
     * @throws SQLException
     * @throws IOException
     */
    public void create(Connection con, Shain shain) throws SQLException, IOException {
        //遉ｾ蜩｡繧堤匳骭ｲ縺吶ｋSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/insert_shain.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            //繝代Λ繝｡繝ｼ繧ｿ繧担QL縺ｫ繧ｻ繝・ヨ
            pstmt.setInt(1, shain.getId());
            pstmt.setString(2, shain.getName());
            pstmt.setString(3, shain.getNamekana());
            pstmt.setInt(4, shain.getEntryyear());
            pstmt.setString(5, shain.getGender());
            pstmt.setInt(6, shain.getPaidLeaveDays());
            pstmt.setString(7, shain.getJobclass());

            //SQL譁・ｒ陦ｨ遉ｺ
            System.out.println(pstmt.toString());

            //SQL螳溯｡・
            pstmt.executeUpdate();
        }
    }

    /**
     * ID繧偵く繝ｼ縺ｫ遉ｾ蜩｡諠・ｱ繧・莉ｶ蜿門ｾ励＠縺ｾ縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @param id 蜿門ｾ励☆繧狗､ｾ蜩｡縺ｮID
     * @return 蜿門ｾ励＠縺溽､ｾ蜩｡縺ｮ諠・ｱ縲りｦ九▽縺九ｉ縺ｪ縺・ｴ蜷医・null縲・
     * @throws SQLException
     * @throws IOException
     */
    public Shain findById(Connection con, int id) throws SQLException, IOException {
        Shain shain = null;

        //遉ｾ蜩｡繧貞叙蠕励☆繧鬼QL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/select_shain.sql");

        //SQL螳溯｡後・貅門ｙ
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //繝代Λ繝｡繝ｼ繧ｿ繧担QL縺ｫ繧ｻ繝・ヨ
            pstmt.setInt(1, id);
            //SQL譁・ｒ陦ｨ遉ｺ
            System.out.println(pstmt.toString());
            //SQL螳溯｡・
            ResultSet rs = pstmt.executeQuery();
            // 蜿門ｾ励＠縺溯｡後′縺ゅｌ縺ｰ
            if (rs.next()) {
                shain = new Shain();
                //蛟､繧貞叙蠕・
                shain.setId(id);
                shain.setName(rs.getString("name"));
                shain.setNamekana(rs.getString("namekana"));
                shain.setGender(rs.getString("gender"));
                shain.setEntryyear(rs.getInt("entry_year"));
                shain.setJobclass(rs.getString("jobclass"));
                shain.setPaidLeaveDays(rs.getInt("paid_leave_days"));
            }
        }
        return shain;
    }

    /**
     * 遉ｾ蜩｡諠・ｱ繧呈峩譁ｰ縺励∪縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @param shain 譖ｴ譁ｰ縺吶ｋ遉ｾ蜩｡諠・ｱ
     * @throws SQLException
     * @throws IOException
     */
    public void update(Connection con, Shain shain) throws SQLException, IOException {
        //遉ｾ蜩｡繧呈峩譁ｰ縺吶ｋSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/update_shain.sql");
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //繝代Λ繝｡繝ｼ繧ｿ繧担QL縺ｫ繧ｻ繝・ヨ
            pstmt.setString(1, shain.getName());
            pstmt.setString(2, shain.getNamekana());
            pstmt.setInt(3, shain.getEntryyear());
            pstmt.setString(4, shain.getGender());
            pstmt.setInt(5, shain.getPaidLeaveDays());
            pstmt.setString(6, shain.getJobclass());
            pstmt.setInt(7, shain.getId());
            //SQL譁・ｒ陦ｨ遉ｺ
            System.out.println(pstmt.toString());
            //SQL螳溯｡・
            pstmt.executeUpdate();
        }
    }

    /**
     * ID繧偵く繝ｼ縺ｫ遉ｾ蜩｡諠・ｱ繧貞炎髯､縺励∪縺吶・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @param id 蜑企勁縺吶ｋ遉ｾ蜩｡縺ｮID
     * @throws SQLException
     * @throws IOException
     */
    public void deleteById(Connection con, int id) throws SQLException, IOException {
        //遉ｾ蜩｡繧貞炎髯､縺吶ｋSQL
        String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/delete_shain.sql");
        //SQL螳溯｡後・貅門ｙ
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            //繝代Λ繝｡繝ｼ繧ｿ繧担QL縺ｫ繧ｻ繝・ヨ
            pstmt.setInt(1, id);
            //SQL譁・ｒ陦ｨ遉ｺ
            System.out.println(pstmt.toString());
            //SQL螳溯｡・
            pstmt.executeUpdate();
        }
    }

    /**
     * 蜷榊燕繧偵く繝ｼ縺ｫ遉ｾ蜩｡諠・ｱ繧呈､懃ｴ｢縺励∪縺呻ｼ磯Κ蛻・ｸ閾ｴ・峨・
     * @param con 繝・・繧ｿ繝吶・繧ｹ謗･邯・
     * @param name 讀懃ｴ｢縺吶ｋ蜷榊燕
     * @return 蜿門ｾ励＠縺溽､ｾ蜩｡縺ｮ諠・ｱ繝ｪ繧ｹ繝・
     * @throws SQLException
     * @throws IOException
     */
    public List<Shain> searchByName(Connection con, String name) throws SQLException, IOException {
        List<Shain> shainList = new ArrayList<>();
        // get_all_shain.sql繧呈ｵ∫畑縺励仝HERE蜿･繧定ｿｽ蜉縺吶ｋ
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
                shain.setPaidLeaveDays(rs.getInt("paid_leave_days"));
                shainList.add(shain);
            }
        }
        return shainList;
    }
}
