package com.example.adapter.out.persistence;

import com.example.application.port.out.LeaveGrantRulePort;
import com.example.entity.LeaveGrantRule;
import com.example.infra.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 有給休暇付与ルールのための永続化アダプタ。
 */
public class LeaveGrantRulePersistenceAdapter implements LeaveGrantRulePort {

    @Override
    public List<LeaveGrantRule> findAll() {
        // TODO: Refactor to read SQL from a file and use a DAO pattern like other adapters.
        String sql = "SELECT id, years_of_service, grant_days FROM leave_grant_rules ORDER BY years_of_service ASC";
        List<LeaveGrantRule> rules = new ArrayList<>();

        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LeaveGrantRule rule = new LeaveGrantRule();
                rule.setId(rs.getInt("id"));
                rule.setYearsOfService(rs.getInt("years_of_service"));
                rule.setGrantDays(rs.getInt("grant_days"));
                rules.add(rule);
            }
        } catch (Exception e) {
            // In a real app, use a more specific exception and logging
            throw new RuntimeException("Failed to find all leave grant rules", e);
        }

        return rules;
    }
}
