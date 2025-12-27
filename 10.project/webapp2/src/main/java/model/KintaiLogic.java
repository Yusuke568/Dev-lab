package model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.example.common.MyUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import beans.CalendarBean;


public class KintaiLogic {
	
	/*
	 * APIより年の祝日リストを取得
	 * 
	 */
	public Map<String, String> fetchHolidaysFromApi(int year) {
	    Map<String, String> holidays = new HashMap<>();
	    String apiUrl = "https://holidays-jp.github.io/api/v1/date.json";

	    try {
	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(apiUrl))
	                .GET()
	                .build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        //「APIの通信が成功したかどうか？」をチェックしてるコード 200
	        if (response.statusCode() == 200) {
	            ObjectMapper mapper = new ObjectMapper();
	            Map<String, String> allHolidays = mapper.readValue(response.body(), new TypeReference<>() {});

	            // 指定された年だけフィルタリング（APIはすべての年を返す）
	            for (Map.Entry<String, String> entry : allHolidays.entrySet()) {
	                if (entry.getKey().startsWith(String.valueOf(year))) {
	                    // yyyy-MM-dd → yyyy/MM/dd に変換
	                    String formattedDate = entry.getKey().replace("-", "/");
	                    holidays.put(formattedDate, entry.getValue());
	                }
	            }
	        } else {
	            System.err.println("祝日APIの取得に失敗しました。ステータス: " + response.statusCode());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return holidays;
	}
	
	
	//指定された年月からカレンダーを作成する。
	 @SuppressWarnings("unlikely-arg-type")
	public List<CalendarBean> generateCalendar(int id,int year, int month) {
	        LocalDate date = LocalDate.of(year, month, 1);

	        // 外部APIから祝日データを取得
	        Map<String, String> holidays = fetchHolidaysFromApi(year);
	        
	        List<CalendarBean> calenderbeanList = new  ArrayList<>();

	        while (date.getMonthValue() == month) {
	        	
	        	CalendarBean calenderbaen = new CalendarBean();
	        	calenderbaen.setId(id);
	            calenderbaen.setKintaidate(Date.valueOf(date));
	            calenderbaen.setWeek(date.getDayOfWeek());
	            calenderbaen.setIsholiday(holidays.containsKey(calenderbaen.getKintaidate()));
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

		Timestamp jobFromTime = null;
		if (kintaiBean.getKintaifrom() != null) {
		    LocalDate fixedDate = LocalDate.of(2000, 1, 1);
		    LocalTime fromLocalTime = kintaiBean.getKintaifrom().toLocalTime();
		    LocalDateTime fromDateTime = LocalDateTime.of(fixedDate, fromLocalTime);
		    jobFromTime = Timestamp.valueOf(fromDateTime);
		}

		// 勤怠TO時間
		Timestamp jobToTime = null;
		if (kintaiBean.getKintaito() != null) {
		    LocalDate fixedDate = LocalDate.of(2000, 1, 1);
		    LocalTime toLocalTime = kintaiBean.getKintaito().toLocalTime();
		    LocalDateTime toDateTime = LocalDateTime.of(fixedDate, toLocalTime);
		    jobToTime = Timestamp.valueOf(toDateTime);
		}
		
		//社員を登録するSQL
		String sql =  MyUtil.loadSqlFromClasspath("sql/kintai/inset_staff_work.sql");
		
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setInt(1, kintaiBean.getId());
			pstmt.setDate(2, Date.valueOf(kintaiBean.getKintaidate()));
			pstmt.setString(3, String.valueOf(kintaiBean.getWeek()));
			pstmt.setTimestamp(4,jobFromTime);
			pstmt.setTimestamp(5, jobToTime);
			pstmt.setInt(6, kintaiBean.getJikangai());
			pstmt.setString(7, kintaiBean.getTekiyoukbn());
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			pstmt.executeUpdate();
		}
	}
	
	
	//勤怠情報を更新
	public void updatetKintai(CalendarBean kintaiBean) throws SQLException, NamingException, IOException {

		Timestamp jobFromTime = null;
		if (kintaiBean.getKintaifrom() != null) {
		    LocalDate fixedDate = LocalDate.of(2000, 1, 1);
		    LocalTime fromLocalTime = kintaiBean.getKintaifrom().toLocalTime();
		    LocalDateTime fromDateTime = LocalDateTime.of(fixedDate, fromLocalTime);
		    jobFromTime = Timestamp.valueOf(fromDateTime);
		}

		// 勤怠TO時間
		Timestamp jobToTime = null;
		if (kintaiBean.getKintaito() != null) {
		    LocalDate fixedDate = LocalDate.of(2000, 1, 1);
		    LocalTime toLocalTime = kintaiBean.getKintaito().toLocalTime();
		    LocalDateTime toDateTime = LocalDateTime.of(fixedDate, toLocalTime);
		    jobToTime = Timestamp.valueOf(toDateTime);
		}
		
		//社員を登録するSQL
		String sql =  MyUtil.loadSqlFromClasspath("sql/kintai/update_staff_work.sql");
		
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setTimestamp(1,jobFromTime);
			pstmt.setTimestamp(2, jobToTime);
			pstmt.setInt(3, kintaiBean.getJikangai());
			pstmt.setString(4, kintaiBean.getTekiyoukbn());
			pstmt.setString(5, kintaiBean.getMemo());
			
			pstmt.setInt(6, kintaiBean.getId());
			pstmt.setDate(7, Date.valueOf(kintaiBean.getKintaidate()));
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
				calenderbean.setWeek(DayOfWeek.valueOf(rs.getString("WORK_WEEK")));
				//calenderbean.setCorrectionid(rs.getInt("CORRECTION_ID"));
				calenderbean.setKintaifrom(rs.getTime("JOB_FROM_TIME"));
				calenderbean.setKintaito(rs.getTime("JOB_TO_TIME"));
				//calenderbean.setCorrectionustime(rs.getInt("CORRECTION_US_TIME"));
				//calenderbean.setCorrectionmidtime(rs.getInt("CORRECTION_MID_TIME"));
				//calenderbean.setIndirecttime(rs.getInt("INDIRECT_TIME"));
				//calenderbean.setTotalworktime(rs.getInt("TOTAL_WORK_TIME"));
				//calenderbean.setTotaldirectworktime(rs.getInt("TOTAL_DIRECT_WORK_TIME"));
				calenderbean.setJikangai(rs.getInt("OVERTIME"));
				calenderbean.setTekiyoukbn(rs.getString("ABSTRACT_ID"));
				calenderbean.setMemo(rs.getString("REMARKS"));
				kintaiList.add(calenderbean);
			}
		}
		return kintaiList;
	}

	// 1日分の勤怠情報を取得
	public CalendarBean getKintaiDay(int staffId, LocalDate date) throws SQLException, NamingException, IOException {
		String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_staff_day.sql");

		try (Connection con = ConnectionBase.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(sql)) {
			
			pstmt.setInt(1, staffId);
			pstmt.setDate(2, Date.valueOf(date));

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				CalendarBean bean = new CalendarBean();
				bean.setId(rs.getInt("STAFF_ID"));
				bean.setKintaidate(rs.getDate("WORK_DATE"));
				bean.setWeek(DayOfWeek.valueOf(rs.getString("WORK_WEEK")));
				bean.setKintaifrom(rs.getTime("JOB_FROM_TIME"));
				bean.setKintaito(rs.getTime("JOB_TO_TIME"));
				bean.setJikangai(rs.getInt("OVERTIME"));
				bean.setTekiyoukbn(rs.getString("ABSTRACT_ID"));
				bean.setMemo(rs.getString("REMARKS"));
				return bean;
			}
		}
		return null; // No record found
	}
}
