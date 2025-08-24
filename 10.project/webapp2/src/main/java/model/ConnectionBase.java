package model;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionBase {
	
	public static Connection getConnection() throws SQLException, NamingException {
		//String localName = "java:comp/env/jdbc/searchman";
		String localName = "java:comp/env/jdbc/sample";
		// コンテキストの生成
		Context context = new InitialContext();
		// コンテキストを検索
		DataSource ds = (DataSource) context.lookup(localName);
		// データベースへ接続
		Connection con = ds.getConnection();
		return con;
	}
}
