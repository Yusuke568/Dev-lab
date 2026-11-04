package com.example.master.adapter.out.persistence;

import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.OrganizationPort;
import com.example.entity.WorkType;
import com.example.leave.domain.model.LeaveGrantRule;
import com.example.master.domain.model.ApprovalRouteSetting;
import com.example.master.domain.model.Department;
import com.example.master.domain.model.JobClass;
import com.example.master.domain.model.PermissionGroup;
import com.example.master.domain.model.StaffOption;
import com.example.master.domain.port.out.MasterPort;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * マスタ管理と組織図の永続化アダプタ。
 *
 * 旧スキーマから移行した DB では、class_master.id が varchar、abstract_master のフラグが integer の場合がある。
 * どちらでも動くよう、それらの値は型を決めずに（Types.OTHER で）渡し、比較は文字列にそろえて行う。
 */
public class MasterPersistenceAdapter implements MasterPort, OrganizationPort {

    // ------------------------------------------------------------------ 役職

    @Override
    public List<JobClass> findJobClasses() {
        String sql = "SELECT c.id, c.name, c.permission_group_id, g.name AS group_name " +
                "FROM class_master c LEFT JOIN permission_group g ON c.permission_group_id = g.id " +
                "ORDER BY CAST(c.id AS varchar)";
        List<JobClass> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new JobClass(Integer.parseInt(rs.getString("id").trim()), rs.getString("name"),
                        nullableInt(rs, "permission_group_id"), rs.getString("group_name")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load job classes.", e);
        }
        list.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        return list;
    }

    @Override
    public void saveJobClass(Integer id, String name, Integer permissionGroupId) {
        try (Connection con = ConnectionBase.getConnection()) {
            if (id == null) {
                int nextId = 0;
                for (JobClass c : findJobClasses()) {
                    nextId = Math.max(nextId, c.getId() + 1);
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO class_master (id, name, permission_group_id) VALUES (?, ?, ?)")) {
                    ps.setObject(1, String.valueOf(nextId), Types.OTHER);
                    ps.setString(2, name);
                    setNullableInt(ps, 3, permissionGroupId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE class_master SET name = ?, permission_group_id = ? WHERE CAST(id AS varchar) = ?")) {
                    ps.setString(1, name);
                    setNullableInt(ps, 2, permissionGroupId);
                    ps.setString(3, String.valueOf(id));
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save job class.", e);
        }
    }

    // ------------------------------------------------------------------ 権限グループ

    @Override
    public List<PermissionGroup> findPermissionGroups() {
        Map<Integer, String> names = new LinkedHashMap<>();
        Map<Integer, Set<String>> features = new LinkedHashMap<>();
        try (Connection con = ConnectionBase.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, name FROM permission_group ORDER BY id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.put(rs.getInt("id"), rs.getString("name"));
                    features.put(rs.getInt("id"), new HashSet<>());
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT group_id, feature_code FROM permission_group_feature");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Set<String> set = features.get(rs.getInt("group_id"));
                    if (set != null) {
                        set.add(rs.getString("feature_code"));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load permission groups.", e);
        }
        List<PermissionGroup> list = new ArrayList<>();
        names.forEach((id, name) -> list.add(new PermissionGroup(id, name, features.get(id))));
        return list;
    }

    @Override
    public int savePermissionGroup(Integer id, String name) {
        try (Connection con = ConnectionBase.getConnection()) {
            if (id == null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO permission_group (name) VALUES (?) RETURNING id")) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        return rs.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE permission_group SET name = ? WHERE id = ?")) {
                ps.setString(1, name);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save permission group.", e);
        }
    }

    @Override
    public void replaceGroupFeatures(int groupId, Set<String> featureCodes) {
        try (Connection con = ConnectionBase.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM permission_group_feature WHERE group_id = ?")) {
                ps.setInt(1, groupId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO permission_group_feature (group_id, feature_code) VALUES (?, ?)")) {
                for (String code : featureCodes) {
                    ps.setInt(1, groupId);
                    ps.setString(2, code);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save permission group features.", e);
        }
    }

    // ------------------------------------------------------------------ 組織図

    @Override
    public List<Department> findDepartments() {
        String sql = "SELECT d.id, d.name, d.parent_id, d.approver_staff_id, d.sort_order, s.name AS approver_name, " +
                "(SELECT COUNT(*) FROM staff_table m WHERE m.department_id = d.id) AS member_count " +
                "FROM department d LEFT JOIN staff_table s ON d.approver_staff_id = s.id " +
                "ORDER BY d.sort_order, d.id";
        List<Department> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Department d = new Department(rs.getInt("id"), rs.getString("name"),
                        nullableInt(rs, "parent_id"), nullableInt(rs, "approver_staff_id"), rs.getInt("sort_order"));
                d.setApproverName(rs.getString("approver_name"));
                d.setMemberCount(rs.getInt("member_count"));
                list.add(d);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load departments.", e);
        }
        return list;
    }

    @Override
    public void saveDepartment(Integer id, String name, Integer parentId, Integer approverStaffId, int sortOrder) {
        String sql = id == null
                ? "INSERT INTO department (name, parent_id, approver_staff_id, sort_order) VALUES (?, ?, ?, ?)"
                : "UPDATE department SET name = ?, parent_id = ?, approver_staff_id = ?, sort_order = ? WHERE id = ?";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            setNullableInt(ps, 2, parentId);
            setNullableInt(ps, 3, approverStaffId);
            ps.setInt(4, sortOrder);
            if (id != null) {
                ps.setInt(5, id);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save department.", e);
        }
    }

    @Override
    public List<StaffOption> findStaffOptions() {
        String sql = "SELECT s.id, s.name, s.department_id, d.name AS department_name " +
                "FROM staff_table s LEFT JOIN department d ON s.department_id = d.id ORDER BY s.id";
        List<StaffOption> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new StaffOption(rs.getInt("id"), rs.getString("name"),
                        nullableInt(rs, "department_id"), rs.getString("department_name")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load staff.", e);
        }
        return list;
    }

    @Override
    public void assignDepartment(int staffId, Integer departmentId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE staff_table SET department_id = ? WHERE id = ?")) {
            setNullableInt(ps, 1, departmentId);
            ps.setInt(2, staffId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign department.", e);
        }
    }

    @Override
    public Integer findDepartmentIdOfStaff(int staffId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT department_id FROM staff_table WHERE id = ?")) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? nullableInt(rs, "department_id") : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load department of staff: " + staffId, e);
        }
    }

    @Override
    public Map<Integer, Department> findAllDepartments() {
        Map<Integer, Department> map = new LinkedHashMap<>();
        for (Department d : findDepartments()) {
            map.put(d.getId(), d);
        }
        return map;
    }

    @Override
    public int findMaxLevels(RequestType requestType) {
        for (ApprovalRouteSetting s : findApprovalRouteSettings()) {
            if (s.getRequestType() == requestType) {
                return s.getMaxLevels();
            }
        }
        return 1;
    }

    // ------------------------------------------------------------------ 適用マスタ

    @Override
    public List<WorkType> findAbstracts() {
        String sql = "SELECT id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday FROM abstract_master ORDER BY id";
        List<WorkType> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                WorkType w = new WorkType();
                w.setId(rs.getInt("id"));
                w.setName(rs.getString("name"));
                w.setWork(rs.getBoolean("is_work"));
                w.setPaid(rs.getBoolean("is_paid"));
                w.setLegalHoliday(rs.getBoolean("is_legal_holiday"));
                w.setPrescribedHoliday(rs.getBoolean("is_prescribed_holiday"));
                list.add(w);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load abstract master.", e);
        }
        return list;
    }

    @Override
    public boolean existsAbstract(int id) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM abstract_master WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check abstract master: " + id, e);
        }
    }

    @Override
    public void insertAbstract(int id, String name, boolean isWork, boolean isPaid, boolean isLegalHoliday, boolean isPrescribedHoliday) {
        String sql = "INSERT INTO abstract_master (name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday, id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        saveAbstract(sql, id, name, isWork, isPaid, isLegalHoliday, isPrescribedHoliday);
    }

    @Override
    public void updateAbstract(int id, String name, boolean isWork, boolean isPaid, boolean isLegalHoliday, boolean isPrescribedHoliday) {
        String sql = "UPDATE abstract_master SET name = ?, is_work = ?, is_paid = ?, is_legal_holiday = ?, " +
                "is_prescribed_holiday = ? WHERE id = ?";
        saveAbstract(sql, id, name, isWork, isPaid, isLegalHoliday, isPrescribedHoliday);
    }

    private void saveAbstract(String sql, int id, String name, boolean isWork, boolean isPaid,
                              boolean isLegalHoliday, boolean isPrescribedHoliday) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            setFlag(ps, 2, isWork);
            setFlag(ps, 3, isPaid);
            setFlag(ps, 4, isLegalHoliday);
            setFlag(ps, 5, isPrescribedHoliday);
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save abstract master.", e);
        }
    }

    // ------------------------------------------------------------------ 有給付与ルール

    @Override
    public List<LeaveGrantRule> findLeaveGrantRules() {
        List<LeaveGrantRule> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT years_of_service, grant_days FROM leave_grant_rules ORDER BY years_of_service");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LeaveGrantRule(rs.getInt("years_of_service"), rs.getInt("grant_days")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load leave grant rules.", e);
        }
        return list;
    }

    @Override
    public void saveLeaveGrantRule(int yearsOfService, int grantDays) {
        try (Connection con = ConnectionBase.getConnection()) {
            int updated;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE leave_grant_rules SET grant_days = ? WHERE years_of_service = ?")) {
                ps.setInt(1, grantDays);
                ps.setInt(2, yearsOfService);
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO leave_grant_rules (years_of_service, grant_days) VALUES (?, ?)")) {
                    ps.setInt(1, yearsOfService);
                    ps.setInt(2, grantDays);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save leave grant rule.", e);
        }
    }

    @Override
    public void deleteLeaveGrantRule(int yearsOfService) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM leave_grant_rules WHERE years_of_service = ?")) {
            ps.setInt(1, yearsOfService);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete leave grant rule.", e);
        }
    }

    // ------------------------------------------------------------------ 会社休日

    @Override
    public List<com.example.master.domain.model.CompanyHoliday> findCompanyHolidays(int year) {
        List<com.example.master.domain.model.CompanyHoliday> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT holiday_date, name FROM company_holiday WHERE holiday_date >= ? AND holiday_date < ? ORDER BY holiday_date")) {
            ps.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.of(year, 1, 1)));
            ps.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.of(year + 1, 1, 1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new com.example.master.domain.model.CompanyHoliday(
                            rs.getDate("holiday_date").toLocalDate(), rs.getString("name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load company holidays.", e);
        }
        return list;
    }

    @Override
    public void saveCompanyHoliday(java.time.LocalDate date, String name) {
        try (Connection con = ConnectionBase.getConnection()) {
            int updated;
            try (PreparedStatement ps = con.prepareStatement("UPDATE company_holiday SET name = ? WHERE holiday_date = ?")) {
                ps.setString(1, name);
                ps.setDate(2, java.sql.Date.valueOf(date));
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO company_holiday (holiday_date, name) VALUES (?, ?)")) {
                    ps.setDate(1, java.sql.Date.valueOf(date));
                    ps.setString(2, name);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save company holiday.", e);
        }
    }

    @Override
    public void deleteCompanyHoliday(java.time.LocalDate date) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM company_holiday WHERE holiday_date = ?")) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete company holiday.", e);
        }
    }

    // ------------------------------------------------------------------ 承認ルート設定

    @Override
    public List<ApprovalRouteSetting> findApprovalRouteSettings() {
        Map<RequestType, Integer> levels = new LinkedHashMap<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT request_type, max_levels FROM approval_route_setting");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    levels.put(RequestType.valueOf(rs.getString("request_type")), rs.getInt("max_levels"));
                } catch (IllegalArgumentException ignored) {
                    // 廃止した申請種別は無視する
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load approval route settings.", e);
        }
        // 未登録の種別も 1 段として画面に出す
        List<ApprovalRouteSetting> list = new ArrayList<>();
        for (RequestType type : RequestType.values()) {
            list.add(new ApprovalRouteSetting(type, levels.getOrDefault(type, 1)));
        }
        return list;
    }

    @Override
    public void saveApprovalRouteSetting(RequestType requestType, int maxLevels) {
        try (Connection con = ConnectionBase.getConnection()) {
            int updated;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE approval_route_setting SET max_levels = ? WHERE request_type = ?")) {
                ps.setInt(1, maxLevels);
                ps.setString(2, requestType.name());
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO approval_route_setting (request_type, max_levels) VALUES (?, ?)")) {
                    ps.setString(1, requestType.name());
                    ps.setInt(2, maxLevels);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save approval route setting.", e);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    /** boolean 列・integer 列のどちらにも入るよう、'1' / '0' を型未指定で渡す。 */
    private static void setFlag(PreparedStatement ps, int index, boolean value) throws SQLException {
        ps.setObject(index, value ? "1" : "0", Types.OTHER);
    }
}
