package com.example.adapter.out.persistence;

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
			// 繝医Λ繝ｳ繧ｶ繧ｯ繧ｷ繝ｧ繝ｳ邂｡逅・ｸ九↓縺ゅｋ蝣ｴ蜷医・縲…lose() 繧堤┌隕悶☆繧輝roxy繧定ｿ斐☆
			return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class<?>[]{Connection.class},
				(proxy, method, args) -> {
					if ("close".equals(method.getName())) {
						return null; // close繧堤┌蜉ｹ蛹・
					}
					return method.invoke(managedCon, args);
				}
			);
		}

		//String localName = "java:comp/env/jdbc/searchman";
		String localName = "java:comp/env/jdbc/kintai";
		// 繧ｳ繝ｳ繝・く繧ｹ繝医・逕滓・
		Context context = new InitialContext();
		// 繧ｳ繝ｳ繝・く繧ｹ繝医ｒ讀懃ｴ｢
		DataSource ds = (DataSource) context.lookup(localName);
		// 繝・・繧ｿ繝吶・繧ｹ縺ｸ謗･邯・
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
