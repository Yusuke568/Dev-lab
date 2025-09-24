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

import beans.AbstractBean;


public class AbstractLogic {
	
	//適用マスタを取得
	public List<AbstractBean> getabstract() throws SQLException, NamingException, IOException {

		//ArrayListの初期化
		List<AbstractBean> AbstractList = new ArrayList<AbstractBean>();

		//社員を取得するSQL
		String sql = MyUtil.loadSqlFromClasspath("sql/kintai/get_abstract.sql");

		//SQL実行の準備
		try (Connection con = ConnectionBase.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);) {
			//SQL文を表示
			System.out.println(pstmt.toString());
			
			//SQL実行
			ResultSet rs = pstmt.executeQuery();
			//取得した行数を繰り返す
			while (rs.next()) {

				AbstractBean calenderbean = new AbstractBean();

				//値を設定
				calenderbean.setId(rs.getInt("ID"));
				calenderbean.setName(rs.getString("NAME"));
				
				AbstractList.add(calenderbean);
			}
		}
		return AbstractList;
	}

}
