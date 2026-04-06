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
     * 譛臥ｵｦ莉倅ｸ弱Ν繝ｼ繝ｫ繧偵☆縺ｹ縺ｦ蜿門ｾ励＠縺ｾ縺吶・
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
     * 謖・ｮ壹＠縺溽､ｾ蜩｡縺ｮ譛臥ｵｦ譌･謨ｰ繧呈峩譁ｰ縺励∪縺吶・
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
     * 謖・ｮ壹＠縺溽､ｾ蜩｡縺ｮ譛臥ｵｦ莨第嚊繧・譌･蜉邂励＠縺ｾ縺吶・
     */
    public void incrementDays(Connection con, int staffId) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/increment_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 謖・ｮ壹＠縺溽､ｾ蜩｡縺ｮ譛臥ｵｦ莨第嚊繧・譌･貂帷ｮ励＠縺ｾ縺吶・
     */
    public void decrementDays(Connection con, int staffId) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/decrement_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 驕ｸ謚槭＠縺溯､・焚縺ｮ遉ｾ蜩｡縺ｫ譛臥ｵｦ繧剃ｸ諡ｬ縺ｧ蜉邂励＠縺ｾ縺吶・
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
     * 隍・焚縺ｮ遉ｾ蜩｡縺ｮ譛臥ｵｦ譌･謨ｰ繧剃ｸ諡ｬ縺ｧ譖ｴ譁ｰ縺励∪縺吶・
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
