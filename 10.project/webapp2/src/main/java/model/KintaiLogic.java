package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import beans.KintaiBean;


public class KintaiLogic {
	

	


	//勤怠情報を登録
	public void insertKintai(KintaiBean kintaiBean) throws SQLException, NamingException {
		//社員を登録するSQL
		String sql = "insert into kintai( UserID, KintaiDate, KintaiFrom, KintaiTo, TekiyouKbn, CreateData, CreateUser) values(?,?,?,?,?,?,?);";
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//パラメータをSQLにセット
			pstmt.setInt(1, kintaiBean.getId());
			pstmt.setDate(2, kintaiBean.getKintaiDate());
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
	public ArrayList<KintaiBean> getAllKintai() throws SQLException, NamingException {

		//ArrayListの初期化
		ArrayList<KintaiBean> kintaiList = new ArrayList<KintaiBean>();

		//社員を取得するSQL
		String sql = "select UserID, KintaiDate, KintaiFrom, KintaiTo, TekiyouKbn from kintai";

		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//SQL文を表示
			System.out.println(pstmt.toString());
			//SQL実行
			ResultSet rs = pstmt.executeQuery();
			//取得した行数を繰り返す
			while (rs.next()) {

				KintaiBean shainbean = new KintaiBean();

				//値を設定
				shainbean.setId(rs.getInt("UserID"));
				shainbean.setKintaiDate(rs.getDate("KintaiDate"));;
				shainbean.setKintaifrom(rs.getTime("KintaiFrom"));
				shainbean.setKintaito(rs.getTime("KintaiTo"));
				shainbean.setTekiyoukbn(rs.getString("TekiyouKbn"));
				kintaiList.add(shainbean);
			}
		}
		return kintaiList;
	}

}
