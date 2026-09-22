package com.example.shared.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Proxy;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionBase {
	
	private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

	public static Connection getConnection() throws SQLException, NamingException {
		Connection managedCon = threadLocalConnection.get();
		if (managedCon != null) {
			// トランザクション管琁E��にある場合�E、close() を無視するProxyを返す
			return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class<?>[]{Connection.class},
				(proxy, method, args) -> {
					if ("close".equals(method.getName())) {
						return null; // closeを無効匁E
					}
					return method.invoke(managedCon, args);
				}
			);
		}

		//String localName = "java:comp/env/jdbc/searchman";
		String localName = "java:comp/env/jdbc/kintai";
		// コンチE��スト�E生�E
		Context context = new InitialContext();
		// コンチE��ストを検索
		DataSource ds = (DataSource) context.lookup(localName);
		// チE�Eタベ�Eスへ接綁E
		Connection con = ds.getConnection();
		return con;
	}

	public static void setCurrentConnection(Connection con) {
		threadLocalConnection.set(con);
	}

	public static void clearCurrentConnection() {
		threadLocalConnection.remove();
	}
}
