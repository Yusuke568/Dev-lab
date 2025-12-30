package dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.common.MyUtil;
import com.example.entity.CalendarDay;

public class KintaiDao {

    /**
     * 指定した社員の勤怠情報がDBに存在するかを確認します。
     */
    public boolean exists(Connection con, int id) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_staff_work_cont.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt("count") > 0;
            }
        }
    }

    /**
     * 指定した社員・年月の勤怠情報を取得します。
     */
    public List<CalendarDay> findByMonth(Connection con, int id, int year, int month)
            throws SQLException, IOException {

        List<CalendarDay> kintaiList = new ArrayList<>();
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_staff_month.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, String.format("%02d", month));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CalendarDay calendarDay = new CalendarDay();
                // (Set properties from ResultSet to calendarDay)
                setCalendarDayFromResultSet(calendarDay, rs);
                kintaiList.add(calendarDay);
            }
        }
        return kintaiList;
    }

    /**
     * 指定した社員・日付の勤怠情報を1件取得します。
     */
    public CalendarDay findByDate(Connection con, int staffId, LocalDate date) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_staff_day.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.setDate(2, Date.valueOf(date));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                CalendarDay bean = new CalendarDay();
                setCalendarDayFromResultSet(bean, rs);
                bean.setTekiyoukbn(rs.getString("TEKIYOUKBN")); // This might be specific to this query
                return bean;
            }
        }
        return null;
    }

    /**
     * 勤怠情報を1件登録します。
     */
    public void create(Connection con, CalendarDay kintai) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/inset_staff_work.sql");
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            setPreparedStatementFromCalendarDay(pstmt, kintai);
            pstmt.executeUpdate();
        }
    }

    /**
     * 勤怠情報を1件更新します。
     */
    public void update(Connection con, CalendarDay kintai) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/update_staff_work.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, kintai.getWeek().toString());
            MyUtil.setObjectOrNull(pstmt, 2, kintai.getCorrectionId());
            pstmt.setTimestamp(3, kintai.getKintaifrom());
            pstmt.setTimestamp(4, kintai.getKintaito());
            MyUtil.setObjectOrNull(pstmt, 5, kintai.getCorrectionUsTime());
            MyUtil.setObjectOrNull(pstmt, 6, kintai.getCorrectionMidTime());
            MyUtil.setObjectOrNull(pstmt, 7, kintai.getIndirectTime());
            MyUtil.setObjectOrNull(pstmt, 8, kintai.getTotalWorkTime());
            MyUtil.setObjectOrNull(pstmt, 9, kintai.getTotalDirectWorkTime());
            MyUtil.setObjectOrNull(pstmt, 10, kintai.getJikangai());
            pstmt.setInt(11, kintai.getAbstractId());
            pstmt.setString(12, kintai.getMemo());
            // WHERE clause parameters
            pstmt.setInt(13, kintai.getId());
            pstmt.setDate(14, Date.valueOf(kintai.getKintaidate()));
            
            pstmt.executeUpdate();
        }
    }

    /**
     * 複数の勤怠情報を一括で登録します。
     */
    public void batchCreate(Connection con, List<CalendarDay> kintaiList) throws SQLException, IOException {
        String sql = MyUtil.loadSqlFromClasspath("sql/kintai/inset_staff_work.sql");

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (CalendarDay kintai : kintaiList) {
                setPreparedStatementFromCalendarDay(pstmt, kintai);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // --- Helper Methods ---

    private void setCalendarDayFromResultSet(CalendarDay calendarDay, ResultSet rs) throws SQLException {
        calendarDay.setId(rs.getInt("STAFF_ID"));
        calendarDay.setKintaidate(rs.getDate("WORK_DATE").toLocalDate());
        calendarDay.setWeek(DayOfWeek.valueOf(rs.getString("WORK_WEEK")));
        calendarDay.setCorrectionId(rs.getObject("CORRECTION_ID", Integer.class));
        calendarDay.setKintaifrom(rs.getTimestamp("JOB_FROM_TIME"));
        calendarDay.setKintaito(rs.getTimestamp("JOB_TO_TIME"));
        calendarDay.setCorrectionUsTime(rs.getObject("CORRECTION_US_TIME", Integer.class));
        calendarDay.setCorrectionMidTime(rs.getObject("CORRECTION_MID_TIME", Integer.class));
        calendarDay.setIndirectTime(rs.getObject("INDIRECT_TIME", Integer.class));
        calendarDay.setTotalWorkTime(rs.getObject("TOTAL_WORK_TIME", Integer.class));
        calendarDay.setTotalDirectWorkTime(rs.getObject("TOTAL_DIRECT_WORK_TIME", Integer.class));
        calendarDay.setJikangai(rs.getInt("OVERTIME"));
        calendarDay.setAbstractId(rs.getInt("ABSTRACT_ID"));
        calendarDay.setMemo(rs.getString("REMARKS"));
    }

    private void setPreparedStatementFromCalendarDay(PreparedStatement pstmt, CalendarDay kintai) throws SQLException {
        pstmt.setInt(1, kintai.getId());
        pstmt.setDate(2, Date.valueOf(kintai.getKintaidate()));
        pstmt.setString(3, kintai.getWeek().toString());
        MyUtil.setObjectOrNull(pstmt, 4, kintai.getCorrectionId());
        pstmt.setTimestamp(5, kintai.getKintaifrom());
        pstmt.setTimestamp(6, kintai.getKintaito());
        MyUtil.setObjectOrNull(pstmt, 7, kintai.getCorrectionUsTime());
        MyUtil.setObjectOrNull(pstmt, 8, kintai.getCorrectionMidTime());
        MyUtil.setObjectOrNull(pstmt, 9, kintai.getIndirectTime());
        MyUtil.setObjectOrNull(pstmt, 10, kintai.getTotalWorkTime());
        MyUtil.setObjectOrNull(pstmt, 11, kintai.getTotalDirectWorkTime());
        MyUtil.setObjectOrNull(pstmt, 12, kintai.getJikangai());
        pstmt.setInt(13, kintai.getAbstractId());
        pstmt.setString(14, kintai.getMemo());
    }
}
