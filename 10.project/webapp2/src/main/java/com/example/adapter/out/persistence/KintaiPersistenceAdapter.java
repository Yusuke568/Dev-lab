package com.example.adapter.out.persistence;

import com.example.application.port.out.KintaiUpdatePort;
import com.example.application.port.out.DailyWorkRecord;
import com.example.common.MyUtil;
import com.example.adapter.out.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class KintaiPersistenceAdapter implements KintaiUpdatePort {

    private static final String UPDATE_SQL = "sql/kintai/update_staff_work.sql";
    private static final String INSERT_SQL = "sql/kintai/insert_staff_work.sql";
    private static final String FIND_BY_DATE_SQL = "sql/kintai/get_staff_day.sql";

    @Override
    public void update(DailyWorkRecord bean) {
        try (Connection con = ConnectionBase.getConnection()) {
            String updateSql = MyUtil.loadSqlFromClasspath(UPDATE_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(updateSql)) {
                pstmt.setString(1, bean.getWeek() != null ? bean.getWeek() : null);
                setIntegerOrNull(pstmt, 2, bean.getCorrectionId());
                pstmt.setTimestamp(3, bean.getKintaifrom());
                pstmt.setTimestamp(4, bean.getKintaito());
                setIntegerOrNull(pstmt, 5, bean.getCorrectionUsTime());
                setIntegerOrNull(pstmt, 6, bean.getCorrectionMidTime());
                setIntegerOrNull(pstmt, 7, bean.getIndirectTime());
                setIntegerOrNull(pstmt, 8, bean.getTotalWorkTime());
                setIntegerOrNull(pstmt, 9, bean.getTotalDirectWorkTime());
                pstmt.setInt(10, bean.getJikangai());
                
                if (bean.getAbstractId() > 0) {
                    pstmt.setInt(11, bean.getAbstractId());
                } else {
                    pstmt.setNull(11, Types.INTEGER);
                }
                
                pstmt.setString(12, bean.getMemo());
                pstmt.setInt(13, bean.getApprovalStatus());
                pstmt.setInt(14, bean.getId());
                pstmt.setDate(15, java.sql.Date.valueOf(bean.getKintaidate()));

                int updatedRows = pstmt.executeUpdate();
                if (updatedRows == 0) {
                    // Update failed because row doesn't exist, do an INSERT
                    insert(con, bean);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update kintai", e);
        }
    }

    private void insert(Connection con, DailyWorkRecord bean) throws Exception {
        String insertSql = MyUtil.loadSqlFromClasspath(INSERT_SQL);
        try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
            pstmt.setInt(1, bean.getId());
            pstmt.setDate(2, java.sql.Date.valueOf(bean.getKintaidate()));
            pstmt.setString(3, bean.getWeek() != null ? bean.getWeek() : null);
            setIntegerOrNull(pstmt, 4, bean.getCorrectionId());
            pstmt.setTimestamp(5, bean.getKintaifrom());
            pstmt.setTimestamp(6, bean.getKintaito());
            setIntegerOrNull(pstmt, 7, bean.getCorrectionUsTime());
            setIntegerOrNull(pstmt, 8, bean.getCorrectionMidTime());
            setIntegerOrNull(pstmt, 9, bean.getIndirectTime());
            setIntegerOrNull(pstmt, 10, bean.getTotalWorkTime());
            setIntegerOrNull(pstmt, 11, bean.getTotalDirectWorkTime());
            pstmt.setInt(12, bean.getJikangai());
            
            if (bean.getAbstractId() > 0) {
                pstmt.setInt(13, bean.getAbstractId());
            } else {
                pstmt.setNull(13, Types.INTEGER);
            }
            
            pstmt.setString(14, bean.getMemo());
            pstmt.setInt(15, bean.getApprovalStatus());
            pstmt.executeUpdate();
        }
    }

    @Override
    public DailyWorkRecord findByDate(int staffId, java.time.LocalDate date) {
        try (Connection con = ConnectionBase.getConnection()) {
            String sql = MyUtil.loadSqlFromClasspath(FIND_BY_DATE_SQL);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, staffId);
                pstmt.setDate(2, java.sql.Date.valueOf(date));
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        DailyWorkRecord bean = new DailyWorkRecord();
                        bean.setId(rs.getInt("STAFF_ID"));
                        java.sql.Date sqlDate = rs.getDate("WORK_DATE");
                        if (sqlDate != null) {
                            bean.setKintaidate(sqlDate.toLocalDate());
                        }
                        bean.setWeek(rs.getString("WORK_WEEK"));
                        bean.setCorrectionId(getIntegerOrNull(rs, "CORRECTION_ID"));
                        bean.setKintaifrom(rs.getTimestamp("JOB_FROM_TIME"));
                        bean.setKintaito(rs.getTimestamp("JOB_TO_TIME"));
                        bean.setCorrectionUsTime(getIntegerOrNull(rs, "CORRECTION_US_TIME"));
                        bean.setCorrectionMidTime(getIntegerOrNull(rs, "CORRECTION_MID_TIME"));
                        bean.setIndirectTime(getIntegerOrNull(rs, "INDIRECT_TIME"));
                        bean.setTotalWorkTime(getIntegerOrNull(rs, "TOTAL_WORK_TIME"));
                        bean.setTotalDirectWorkTime(getIntegerOrNull(rs, "TOTAL_DIRECT_WORK_TIME"));
                        bean.setJikangai(rs.getInt("OVERTIME"));
                        bean.setAbstractId(rs.getInt("ABSTRACT_ID"));
                        bean.setMemo(rs.getString("REMARKS"));
                        
                        // Check if column exists, to be safe. But we know we are adding it.
                        // However, FIND_BY_DATE_SQL needs to be updated too if it's a SELECT * or SELECT specific columns.
                        try {
                            bean.setApprovalStatus(rs.getInt("APPROVAL_STATUS"));
                        } catch (java.sql.SQLException e) {
                            // Column might not exist in old queries, ignore
                        }
                        return bean;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find kintai by date", e);
        }
        return null;
    }

    private void setIntegerOrNull(PreparedStatement pstmt, int index, Integer value) throws java.sql.SQLException {
        if (value != null) {
            pstmt.setInt(index, value);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }

    private Integer getIntegerOrNull(ResultSet rs, String columnLabel) throws java.sql.SQLException {
        int value = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }
}
