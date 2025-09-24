package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import beans.CalendarBean;
import model.ConnectionBase;
import model.KintaiLogic;

/**
 * Servlet implementation class ShainInsert
 */
@WebServlet("/Kintaiinsert")
public class KintaiInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KintaiInsert() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		while ((line = request.getReader().readLine()) != null) {
		    jsonBuffer.append(line);
		}
		String jsonData = jsonBuffer.toString(); // ← これが必要！
		

	    ObjectMapper mapper = new ObjectMapper();
	    List<CalendarBean> calendarBeanList = mapper.readValue(jsonData, new TypeReference<List<CalendarBean>>() {});
	    
	    KintaiLogic kintailogic = new KintaiLogic();

		
		Connection con = null;
		

		try {
		    con = ConnectionBase.getConnection();
		    con.setAutoCommit(false);
		    
		    for (CalendarBean bean : calendarBeanList) {
		        //kintailogic.insertKintai(bean, con); // ← Connectionを渡して保存！
		    	bean.setId(668);
		    	kintailogic.updatetKintai(bean);
		    }

		    con.commit();
		} catch (Exception e) {
		    e.printStackTrace();
		    if (con != null) {
		        try {
		            con.rollback();
		        } catch (SQLException ex) {
		            ex.printStackTrace();
		        }
		    }
		} finally {
		    if (con != null) {
		        try {
		            con.close();
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
		}
	}
}
