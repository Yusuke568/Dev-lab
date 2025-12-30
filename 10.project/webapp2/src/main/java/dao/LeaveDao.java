package dao;

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
     * 有給付与ルールをすべて取得します。
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
     * 指定した社員の有給日数を更新します。
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
     * 指定した社員の有給休暇を1日加算します。
     */
    public void incrementDays(Connection con, int staffId) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/increment_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 指定した社員の有給休暇を1日減算します。
     */
    public void decrementDays(Connection con, int staffId) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/leave/decrement_days.sql");
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 選択した複数の社員に有給を一括で加算します。
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
     * 複数の社員の有給日数を一括で更新します。
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
