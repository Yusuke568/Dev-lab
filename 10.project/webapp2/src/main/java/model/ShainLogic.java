package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;


import com.example.common.MyUtil;

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
			pstmt.setString(5, shainbean.getJobclass());
			pstmt.setInt(6, shainbean.getId());
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
			pstmt.setString(6, shainBean.getJobclass());
			
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
				shainList.add(shainbean);
			}
		}
		return shainList;
	}

}
