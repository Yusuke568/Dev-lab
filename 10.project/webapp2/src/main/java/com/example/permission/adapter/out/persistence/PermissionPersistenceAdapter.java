package com.example.permission.adapter.out.persistence;

import com.example.permission.domain.port.out.LoadUserFeaturesPort;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 権限グループから社員の機能を読み込む永続化アダプタ。
 */
public class PermissionPersistenceAdapter implements LoadUserFeaturesPort {

    private static final String FIND_GROUP_SQL =
            "SELECT c.permission_group_id FROM staff_table s JOIN class_master c ON s.class_id = c.id WHERE s.id = ?";
    private static final String FIND_FEATURES_SQL =
            "SELECT feature_code FROM permission_group_feature WHERE group_id = ?";

    @Override
    public Optional<Set<String>> loadFeatureCodes(int staffId) {
        try (Connection con = ConnectionBase.getConnection()) {
            Integer groupId = null;
            try (PreparedStatement ps = con.prepareStatement(FIND_GROUP_SQL)) {
                ps.setInt(1, staffId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        groupId = rs.wasNull() ? null : id;
                    }
                }
            }
            if (groupId == null) {
                return Optional.empty();
            }

            Set<String> codes = new HashSet<>();
            try (PreparedStatement ps = con.prepareStatement(FIND_FEATURES_SQL)) {
                ps.setInt(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        codes.add(rs.getString(1));
                    }
                }
            }
            return Optional.of(codes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load features for staff: " + staffId, e);
        }
    }
}
