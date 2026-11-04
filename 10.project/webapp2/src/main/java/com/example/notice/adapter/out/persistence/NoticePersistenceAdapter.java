package com.example.notice.adapter.out.persistence;

import com.example.common.MyUtil;
import com.example.notice.domain.model.Notice;
import com.example.notice.domain.model.NoticeDepartment;
import com.example.notice.domain.port.out.NoticePort;
import com.example.shared.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * お知らせの永続化アダプタ。
 */
public class NoticePersistenceAdapter implements NoticePort {

    private static final String SQL_DIR = "sql/notice/";

    @Override
    public List<Notice> findVisibleFor(int staffId, LocalDate today) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(MyUtil.loadSqlFromClasspath(SQL_DIR + "find_visible_notices.sql"))) {
            ps.setInt(1, staffId);
            ps.setDate(2, java.sql.Date.valueOf(today));
            return readNotices(ps);
        } catch (Exception e) {
            // お知らせのテーブルがまだない（マイグレーション前）などでも、勤怠画面は開けるようにする
            System.err.println("Warning: Failed to load notices. " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Notice> findByAuthor(int authorStaffId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(MyUtil.loadSqlFromClasspath(SQL_DIR + "find_author_notices.sql"))) {
            ps.setInt(1, authorStaffId);
            return readNotices(ps);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load notices of author.", e);
        }
    }

    @Override
    public List<NoticeDepartment> findManagedDepartments(int approverStaffId) {
        List<NoticeDepartment> list = new ArrayList<>();
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(MyUtil.loadSqlFromClasspath(SQL_DIR + "find_managed_departments.sql"))) {
            ps.setInt(1, approverStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new NoticeDepartment(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load managed departments.", e);
        }
        return list;
    }

    @Override
    public void insert(int authorStaffId, int departmentId, String body, LocalDate expiresOn) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(MyUtil.loadSqlFromClasspath(SQL_DIR + "insert_notice.sql"))) {
            ps.setInt(1, authorStaffId);
            ps.setInt(2, departmentId);
            ps.setString(3, body);
            if (expiresOn == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, java.sql.Date.valueOf(expiresOn));
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save notice.", e);
        }
    }

    @Override
    public boolean delete(int noticeId, int authorStaffId) {
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(MyUtil.loadSqlFromClasspath(SQL_DIR + "delete_notice.sql"))) {
            ps.setInt(1, noticeId);
            ps.setInt(2, authorStaffId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete notice.", e);
        }
    }

    private static List<Notice> readNotices(PreparedStatement ps) throws SQLException {
        List<Notice> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.sql.Date expires = rs.getDate("expires_on");
                java.sql.Timestamp created = rs.getTimestamp("created_at");
                list.add(new Notice(rs.getInt("id"), rs.getInt("author_staff_id"), rs.getString("author_name"),
                        rs.getInt("department_id"), rs.getString("department_name"), rs.getString("body"),
                        expires == null ? null : expires.toLocalDate(),
                        created == null ? null : created.toLocalDateTime()));
            }
        }
        return list;
    }
}
