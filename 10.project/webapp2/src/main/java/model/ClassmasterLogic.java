package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import com.example.common.MyUtil;

import beans.ClassmasterBean;
public class ClassmasterLogic {

	//役職リストを取得
	public ArrayList<ClassmasterBean> getAllClassmaster() throws SQLException, NamingException, IOException {

		//ArrayListの初期化
		ArrayList<ClassmasterBean> classmasterList = new ArrayList<ClassmasterBean>();

		//社員を取得するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/classmaster/get_all_class.sql");

		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//SQL文をコンソール表示
			System.out.println(pstmt.toString());
			//SQL実行
			ResultSet rs = pstmt.executeQuery();
			//取得した行数を繰り返す
			while (rs.next()) {

				ClassmasterBean classmasterbean = new ClassmasterBean();

				//値を設定
				classmasterbean.setId(rs.getString("ID"));
				classmasterbean.setName(rs.getString("NAME"));
				classmasterList.add(classmasterbean);
			}
		}
		return classmasterList;
	}

}
