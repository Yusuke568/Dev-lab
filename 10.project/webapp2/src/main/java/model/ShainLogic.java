package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.example.common.MyUtil;

import beans.LeaveGrantRuleBean;
import beans.ShainBean;

public class ShainLogic {
	
	//社員を削除
	public void deleteShain(int id) throws SQLException, NamingException, IOException {

		//社員を削除するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/delete_shain.sql");
		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setInt(1, id);
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			pstmt.executeUpdate();
		}
	}
	

	//社員情報をを更新
	public  void updateShain(ShainBean shainbean) throws SQLException, NamingException, IOException {

		//社員を更新するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/update_shain.sql");
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setString(1,shainbean.getName());
			pstmt.setString(2, shainbean.getNamekana());
			pstmt.setInt(3, shainbean.getEntryyear());
			pstmt.setString(4, shainbean.getGender());
			pstmt.setInt(5, shainbean.getPaidLeaveDays());
			pstmt.setString(6, shainbean.getJobclass());
			pstmt.setInt(7, shainbean.getId());
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			pstmt.executeUpdate();
		}
	}

	

	//社員IDを取得
	public ShainBean getShainBean(int id) throws SQLException, NamingException, IOException {
		
		//社員情報初期化
		ShainBean shainBean = new ShainBean();

		//社員を取得するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/select_shain.sql");

		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setInt(1, id);
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			ResultSet rs = pstmt.executeQuery();
			//取得した行数を繰り返す
			while (rs.next()) {
				//値を取得
				shainBean.setId(id);
				shainBean.setName(rs.getString("name"));
				shainBean.setNamekana(rs.getString("namekana"));
				shainBean.setGender(rs.getString("gender"));
				shainBean.setEntryyear(rs.getInt("entry_year"));
				shainBean.setJobclass(rs.getString("jobclass"));
				shainBean.setPaidLeaveDays(rs.getInt("paid_leave_days"));
			}
		}
		return shainBean;
	}




	//社員を登録
	public void insertShain(ShainBean shainBean) throws SQLException, NamingException, IOException {
		//社員を登録するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/insert_shain.sql");
		
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {

			//パラメータをSQLにセット
			pstmt.setInt(1, shainBean.getId());
			pstmt.setString(2, shainBean.getName());
			pstmt.setString(3, shainBean.getNamekana());
			pstmt.setInt(4, shainBean.getEntryyear());
			pstmt.setString(5, shainBean.getGender());
			pstmt.setInt(6, shainBean.getPaidLeaveDays());
			pstmt.setString(7, shainBean.getJobclass());
			
			//SQL文を表示
			System.out.println(pstmt.toString());
			
			//SQL実行
			pstmt.executeUpdate();
		}
	}

	//全社員を取得
	public ArrayList<ShainBean> getAllShain() throws SQLException, NamingException, IOException {

		//ArrayListの初期化
		ArrayList<ShainBean> shainList = new ArrayList<ShainBean>();

		//社員を取得するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql");

		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//SQL文をコンソール表示
			System.out.println(pstmt.toString());
			//SQL実行
			ResultSet rs = pstmt.executeQuery();
			//取得した行数を繰り返す
			while (rs.next()) {

				ShainBean shainbean = new ShainBean();

				//値を設定
				shainbean.setId(rs.getInt("id"));
				shainbean.setName(rs.getString("name"));
				shainbean.setNamekana(rs.getString("namekana"));
				shainbean.setEntryyear(rs.getInt("entry_year"));
				shainbean.setGender(rs.getString("gender"));
				shainbean.setJobclass(rs.getString("jobclass"));
				shainbean.setPaidLeaveDays(rs.getInt("paid_leave_days"));
				shainList.add(shainbean);
			}
		}
		return shainList;
	}

	// 全社員の情報を取得（勤続年数も計算）
	public ArrayList<ShainBean> getAllShainWithLeaveInfo() throws SQLException, NamingException, IOException {

		ArrayList<ShainBean> shainList = new ArrayList<>();
		String sql = MyUtil.loadSqlFromClasspath("sql/shainlist/get_all_shain.sql");

		try (Connection con = ConnectionBase.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(sql)) {
			
			ResultSet rs = pstmt.executeQuery();

			int currentYear = java.time.Year.now().getValue();

			while (rs.next()) {
				ShainBean shainbean = new ShainBean();

				shainbean.setId(rs.getInt("id"));
				shainbean.setName(rs.getString("name"));
				shainbean.setNamekana(rs.getString("namekana"));
				int entryYear = rs.getInt("entry_year");
				shainbean.setEntryyear(entryYear);
				shainbean.setGender(rs.getString("gender"));
				shainbean.setJobclass(rs.getString("jobclass"));
				shainbean.setPaidLeaveDays(rs.getInt("paid_leave_days"));
				
				// 勤続年数の計算
				shainbean.setYearsOfService(currentYear - entryYear);

				shainList.add(shainbean);
			}
		}
		return shainList;
	}

	// 勤続年数に応じた有給付与を実行
	public void grantLeaveByYearsOfService() throws SQLException, NamingException, IOException {
		// 1. 付与ルールを取得
		List<LeaveGrantRuleBean> rules = getLeaveGrantRules();
		// 2. 全社員情報を取得
		List<ShainBean> employees = getAllShainWithLeaveInfo();

		String sql = "UPDATE staff_table SET PAID_LEAVE_DAYS = ? WHERE ID = ?";
		
		try (Connection con = ConnectionBase.getConnection()) {
			con.setAutoCommit(false); // トランザクション開始
			
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				for (ShainBean employee : employees) {
					int years = employee.getYearsOfService();
					int grantDays = 0;

					// 勤続年数に最も近いルールを見つける
					for (LeaveGrantRuleBean rule : rules) {
						if (years >= rule.getYearsOfService()) {
							grantDays = rule.getGrantDays();
						} else {
							break; // Rules should be ordered by years_of_service
						}
					}
					
					if (grantDays > 0) {
						pstmt.setInt(1, grantDays);
						pstmt.setInt(2, employee.getId());
						pstmt.addBatch();
					}
				}
				pstmt.executeBatch();
				con.commit(); // トランザクション確定
			} catch (SQLException e) {
				con.rollback(); // エラー発生時にロールバック
				throw e;
			}
		}
	}

	// 有給付与ルールを取得するヘルパーメソッド
	private List<LeaveGrantRuleBean> getLeaveGrantRules() throws SQLException, NamingException {
		List<LeaveGrantRuleBean> rules = new ArrayList<>();
		// a new sql file should be created for this
		String sql = "SELECT id, years_of_service, grant_days FROM leave_grant_rules ORDER BY years_of_service ASC";
		
		try (Connection con = ConnectionBase.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {
			
			while (rs.next()) {
				LeaveGrantRuleBean rule = new LeaveGrantRuleBean();
				rule.setId(rs.getInt("id"));
				rule.setYearsOfService(rs.getInt("years_of_service"));
				rule.setGrantDays(rs.getInt("grant_days"));
				rules.add(rule);
			}
		}
		return rules;
	}

	// 選択した社員に有給を付与（加算）
	public void addLeaveDaysToSelected(String[] ids, int days) throws SQLException, NamingException {
		String sql = "UPDATE staff_table SET PAID_LEAVE_DAYS = PAID_LEAVE_DAYS + ? WHERE ID = ?";
		
		try (Connection con = ConnectionBase.getConnection()) {
			con.setAutoCommit(false);
			
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				for (String id : ids) {
					pstmt.setInt(1, days);
					pstmt.setInt(2, Integer.parseInt(id));
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				con.commit();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			}
		}
	}

	// 個別社員の有給日数を更新（絶対値で設定）
	public void updateLeaveDays(int id, int days) throws SQLException, NamingException {
		String sql = "UPDATE staff_table SET PAID_LEAVE_DAYS = ? WHERE ID = ?";
		
		try (Connection con = ConnectionBase.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(sql)) {
			
			pstmt.setInt(1, days);
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
		}
	}

	// 有給休暇を1日減算する
	public void decrementLeaveDay(int staffId) throws SQLException, NamingException {
		String sql = "UPDATE staff_table SET PAID_LEAVE_DAYS = PAID_LEAVE_DAYS - 1 WHERE ID = ?";
		
		try (Connection con = ConnectionBase.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(sql)) {
			
			pstmt.setInt(1, staffId);
			pstmt.executeUpdate();
		}
	}

	// 有給休暇を1日加算する
	public void incrementLeaveDay(int staffId) throws SQLException, NamingException {
		String sql = "UPDATE staff_table SET PAID_LEAVE_DAYS = PAID_LEAVE_DAYS + 1 WHERE ID = ?";
		
		try (Connection con = ConnectionBase.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(sql)) {
			
			pstmt.setInt(1, staffId);
			pstmt.executeUpdate();
		}
	}
}
