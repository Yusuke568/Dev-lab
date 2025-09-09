package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import com.example.common.MyUtil;

import beans.CalendarBean;


public class KintaiLogic {
	
	
	//指定された年月からカレンダーを作成する。
	 public List<CalendarBean> generateCalendar(int id,int year, int month) {
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	        LocalDate date = LocalDate.of(year, month, 1);

	        // TODO:仮の祝日データ（本物はAPIやライブラリで取得可能）
	        Set<String> holidays = new HashSet<>();
	        holidays.add("2025/01/01"); // 元日
	        holidays.add("2025/01/13"); // 成人の日
	        holidays.add("2025/02/11"); // 建国記念の日
	        holidays.add("2025/02/23"); // 天皇誕生日
	        holidays.add("2025/02/24"); // 振替休日
	        holidays.add("2025/03/20"); // 春分の日
	        holidays.add("2025/04/29"); // 昭和の日
	        holidays.add("2025/05/03"); // 憲法記念日
	        holidays.add("2025/05/04"); // みどりの日
	        holidays.add("2025/05/05"); // こどもの日
	        holidays.add("2025/05/06"); // 振替休日
	        holidays.add("2025/07/21"); // 海の日
	        holidays.add("2025/08/11"); // 山の日
	        holidays.add("2025/09/15"); // 敬老の日
	        holidays.add("2025/09/23"); // 秋分の日
	        holidays.add("2025/10/13"); // スポーツの日
	        holidays.add("2025/11/03"); // 文化の日
	        holidays.add("2025/11/23"); // 勤労感謝の日
	        holidays.add("2025/11/24"); // 振替休日
	        holidays.add("2025/12/23"); // 天皇誕生日
	        
	        List<CalendarBean> calenderbeanList = new  ArrayList<>();

	        while (date.getMonthValue() == month) {
	        	
	        	CalendarBean calenderbaen = new CalendarBean();
	        	calenderbaen.setId(id);
	            calenderbaen.setKintaidate(Date.valueOf(date));
	            calenderbaen.setWeek(date.getDayOfWeek());
	            calenderbaen.setIsholiday(holidays.contains(calenderbaen.getKintaidate()));
	            calenderbeanList.add(calenderbaen);
	            date = date.plusDays(1);
	        }
			return calenderbeanList;
	 }
	 
	 //勤怠情報が存在するか確認
	 public int isStaffidwork(int id) throws SQLException, NamingException, IOException {
		 
		//指定されたIDが勤怠テーブルに存在しているかSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_staff_work_cont.sql");
		
		try(Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);){
		
			//パラメータをSQLにセット
			pstmt.setInt(1, id);
			//SQL文を表示
			System.out.println(pstmt.toString());
			
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			if (rs.next()) {
			    count = rs.getInt("count");
			}
			
			return (count == 0) ? -1 : count;
			
		}
	 }


	//勤怠情報を登録
	public void insertKintai(CalendarBean kintaiBean) throws SQLException, NamingException, IOException {
		//社員を登録するSQL
		String sql =  MyUtil.loadSqlFromClasspath("sql/kintai/inset_staff_work.sql");
		
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setInt(1, kintaiBean.getId());
			pstmt.setDate(2, Date.valueOf(kintaiBean.getKintaidate()));
			pstmt.setTime(3, kintaiBean.getKintaifrom());
			pstmt.setTime(4, kintaiBean.getKintaito());
			pstmt.setString(5, kintaiBean.getTekiyoukbn());
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			pstmt.executeUpdate();
		}
	}

	//勤怠情報を取得
	public List<CalendarBean> getmonthKintai(int id,int year,int month) throws SQLException, NamingException, IOException {

		//ArrayListの初期化
		List<CalendarBean> kintaiList = new ArrayList<CalendarBean>();

		//社員を取得するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_staff_month.sql");

		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//SQL文を表示
			System.out.println(pstmt.toString());
			pstmt.setInt(1,id);
			pstmt.setString(2, String.valueOf(year));
			pstmt.setString(3, String.format("%02d", month));
			
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			ResultSet rs = pstmt.executeQuery();
			//取得した行数を繰り返す
			while (rs.next()) {

				CalendarBean calenderbean = new CalendarBean();

				//値を設定
				calenderbean.setId(rs.getInt("STAFF_ID"));
				calenderbean.setKintaidate(rs.getDate("WORK_DATE"));
				calenderbean.setKintaifrom(rs.getTime("JOB_FROM_TIME"));
				calenderbean.setKintaito(rs.getTime("JOB_TO_TIME"));
				calenderbean.setTekiyoukbn(rs.getString("ABSTRACT_ID"));
				System.out.println(calenderbean.getKintaifrom());
				System.out.println(calenderbean.getKintaito());
				kintaiList.add(calenderbean);
			}
		}
		return kintaiList;
	}

}
