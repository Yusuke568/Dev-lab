package com.example.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;
import com.example.entity.LeaveGrantRule;
import com.example.entity.Shain;

public class LeaveDao {

    /**
     * 隴幄・・ｵ・ｦ闔牙・ｽｸ蠑ｱﾎ晉ｹ晢ｽｼ郢晢ｽｫ郢ｧ蛛ｵ笘・ｸｺ・ｹ邵ｺ・ｦ陷ｿ髢・ｾ蜉ｱ・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     */
    public List<LeaveGrantRule> findAllRules(Connection con) throws SQLException, IOException {
        List<LeaveGrantRule> rules = new ArrayList<>();
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/find_all_rules.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                LeaveGrantRule rule = new LeaveGrantRule();
                rule.setId(rs.getInt("id"));
                rule.setYearsOfService(rs.getInt("years_of_service"));
                rule.setGrantDays(rs.getInt("grant_days"));
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * 隰悶・・ｮ螢ｹ・邵ｺ貅ｽ・､・ｾ陷ｩ・｡邵ｺ・ｮ隴幄・・ｵ・ｦ隴鯉ｽ･隰ｨ・ｰ郢ｧ蜻亥ｳｩ隴・ｽｰ邵ｺ蜉ｱ竏ｪ邵ｺ蜷ｶﾂ繝ｻ
     */
    public void updateDays(Connection con, int id, int days) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/update_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * 隰悶・・ｮ螢ｹ・邵ｺ貅ｽ・､・ｾ陷ｩ・｡邵ｺ・ｮ隴幄・・ｵ・ｦ闔ｨ隨ｬ蝴顔ｹｧ繝ｻ隴鯉ｽ･陷会｣ｰ驍ょ干・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     */
    public void incrementDays(Connection con, int staffId) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/increment_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 隰悶・・ｮ螢ｹ・邵ｺ貅ｽ・､・ｾ陷ｩ・｡邵ｺ・ｮ隴幄・・ｵ・ｦ闔ｨ隨ｬ蝴顔ｹｧ繝ｻ隴鯉ｽ･雋ょｸｷ・ｮ蜉ｱ・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     */
    public void decrementDays(Connection con, int staffId) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/decrement_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 鬩包ｽｸ隰壽ｧｭ・邵ｺ貅ｯ・､繝ｻ辟夂ｸｺ・ｮ驕会ｽｾ陷ｩ・｡邵ｺ・ｫ隴幄・・ｵ・ｦ郢ｧ蜑・ｽｸﾂ隲｡・ｬ邵ｺ・ｧ陷会｣ｰ驍ょ干・邵ｺ・ｾ邵ｺ蜷ｶﾂ繝ｻ
     */
    public void addDaysToSelected(Connection con, String[] ids, int days) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/add_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (String id : ids) {
                pstmt.setInt(1, days);
                pstmt.setInt(2, Integer.parseInt(id));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * 髫阪・辟夂ｸｺ・ｮ驕会ｽｾ陷ｩ・｡邵ｺ・ｮ隴幄・・ｵ・ｦ隴鯉ｽ･隰ｨ・ｰ郢ｧ蜑・ｽｸﾂ隲｡・ｬ邵ｺ・ｧ隴厄ｽｴ隴・ｽｰ邵ｺ蜉ｱ竏ｪ邵ｺ蜷ｶﾂ繝ｻ
     */
    public void batchUpdateDays(Connection con, List<Shain> employees) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/update_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (Shain employee : employees) {
                pstmt.setInt(1, employee.getPaidLeaveDays());
                pstmt.setInt(2, employee.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}
