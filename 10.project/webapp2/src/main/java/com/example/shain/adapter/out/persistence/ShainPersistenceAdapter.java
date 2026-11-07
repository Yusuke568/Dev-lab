package com.example.shain.adapter.out.persistence;

import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;
import com.example.dao.ShainDao;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社員データのための永続化アダプタ（完全ヘキサゴナル版）。
 */
public class ShainPersistenceAdapter implements ShainPort {

    private final ShainDao shainDao;

    public ShainPersistenceAdapter() {
        this.shainDao = new ShainDao();
    }

    @Override
    public List<Shain> findAll() {
        try (Connection con = ConnectionBase.getConnection()) {
            return shainDao.findAll(con).stream()
                    .map(this::mapToDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all shain", e);
        }
    }

    @Override
    public Optional<Shain> findById(ShainId id) {
        try (Connection con = ConnectionBase.getConnection()) {
            com.example.entity.Shain entity = shainDao.findById(con, id.getIntValue());
            return Optional.ofNullable(entity).map(this::mapToDomain);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find shain by id: " + id, e);
        }
    }

    @Override
    public void update(Shain shain) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.update(con, mapToEntity(shain));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update shain: " + shain.getId(), e);
        }
    }

    @Override
    public void create(Shain shain) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.create(con, mapToEntity(shain));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shain", e);
        }
    }

    @Override
    public void deleteById(ShainId id) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.deleteById(con, id.getIntValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete shain: " + id, e);
        }
    }

    @Override
    public void assignDepartment(ShainId id, Integer departmentId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     com.example.common.MyUtil.loadSqlFromClasspath("sql/shainlist/update_shain_department.sql"))) {
            if (departmentId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, departmentId);
            }
            ps.setInt(2, id.getIntValue());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign department: " + id, e);
        }
    }

    /**
     * 社員IDを持つ列（テーブル名, 列名）。社員IDを変えるときにすべて書き換える。
     * テーブルや列を追加したら、ここにも足すこと。
     */
    private static final String[][] STAFF_ID_COLUMNS = {
            {"staff_table", "id"},
            {"work_month_table", "staff_id"},
            {"kintai_approval_history", "staff_id"},
            {"kintai_approval_history", "decided_by"},
            {"overtime_request", "staff_id"},
            {"overtime_request", "decided_by"},
            {"weekly_work_report", "staff_id"},
            {"department", "approver_staff_id"},
            {"leave_request", "staff_id"},
            {"approval_step", "approver_staff_id"},
            {"notice", "author_staff_id"},
            {"staff_department", "staff_id"},
            {"department_approver", "staff_id"},
            {"approval_route_step", "staff_id"},
    };

    @Override
    public void changeId(ShainId from, ShainId to) {
        try (Connection con = ConnectionBase.getConnection()) {
            for (String[] tc : STAFF_ID_COLUMNS) {
                // マイグレーション前でまだないテーブルは飛ばす
                if (!tableExists(con, tc[0])) {
                    continue;
                }
                // テーブル名・列名は上の固定の一覧から組み立てる（外からの入力は入らない）
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE " + tc[0] + " SET " + tc[1] + " = ? WHERE " + tc[1] + " = ?")) {
                    ps.setInt(1, to.getIntValue());
                    ps.setInt(2, from.getIntValue());
                    ps.executeUpdate();
                }
            }
            // 承認ルートの「社員」への割り当て（target_id は部署IDのこともあるので種類で絞る）
            if (tableExists(con, "approval_route_assignment")) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE approval_route_assignment SET target_id = ? WHERE target_type = 'STAFF' AND target_id = ?")) {
                    ps.setInt(1, to.getIntValue());
                    ps.setInt(2, from.getIntValue());
                    ps.executeUpdate();
                }
            }
            // ログインのユーザー名は社員IDと同じ文字列
            try (PreparedStatement ps = con.prepareStatement("UPDATE login_table SET username = ? WHERE username = ?")) {
                ps.setString(1, to.getValue());
                ps.setString(2, from.getValue());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to change shain id: " + from + " -> " + to, e);
        }
    }

    private static boolean tableExists(Connection con, String table) throws java.sql.SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    @Override
    public ShainId nextId() {
        // IDの自動採番ロジック（実際にはDBのシーケンス等を利用）
        String sql = "SELECT MAX(id) FROM staff_table";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return ShainId.of(rs.getInt(1) + 1);
            }
            return ShainId.of(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get next shain id", e);
        }
    }

    /**
     * 外部のEntity（Bean）をドメインモデルに変換します。
     */
    private Shain mapToDomain(com.example.entity.Shain entity) {
        return new Shain(
                ShainId.of(entity.getId()),
                entity.getName(),
                entity.getNamekana(),
                entity.getEntryyear(),
                entity.getGender(),
                entity.getJobclass(),
                entity.getPaidLeaveDays(),
                // 勤続年数は入社年から数える（DB には持たない）
                Shain.yearsOfServiceFrom(entity.getEntryyear(), java.time.Year.now().getValue())
        );
    }

    /**
     * ドメインモデルを外部のEntity（Bean）に変換します。
     */
    private com.example.entity.Shain mapToEntity(Shain domain) {
        com.example.entity.Shain entity = new com.example.entity.Shain();
        entity.setId(domain.getId().getIntValue());
        entity.setName(domain.getName());
        entity.setNamekana(domain.getNamekana());
        entity.setEntryyear(domain.getEntryYear());
        entity.setGender(domain.getGender());
        entity.setJobclass(domain.getJobClass());
        entity.setPaidLeaveDays(domain.getPaidLeaveDays());
        entity.setYearsOfService(domain.getYearsOfService());
        return entity;
    }
}
