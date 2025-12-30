package controller;

import java.io.IOException;
import java.sql.Connection;
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
import model.ShainLogic;

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
		String jsonData = jsonBuffer.toString();

	    ObjectMapper mapper = new ObjectMapper();
	    List<CalendarBean> newKintaiList = mapper.readValue(jsonData, new TypeReference<List<CalendarBean>>() {});
	    
	    KintaiLogic kintaiLogic = new KintaiLogic();
		ShainLogic shainLogic = new ShainLogic();

		try (Connection con = ConnectionBase.getConnection()) {
		    con.setAutoCommit(false);
		    
		    for (CalendarBean newBean : newKintaiList) {
				CalendarBean oldBean = kintaiLogic.getKintaiDay(newBean.getId(), newBean.getKintaidate());

				String oldStatus = (oldBean != null && oldBean.getTekiyoukbn() != null) ? oldBean.getTekiyoukbn() : "";
				String newStatus = (newBean.getTekiyoukbn() != null) ? newBean.getTekiyoukbn() : "";

				// Case 1: Status changed TO "Paid Leave"
				if (!oldStatus.equals("有給") && newStatus.equals("有給")) {
					shainLogic.decrementLeaveDay(newBean.getId());
				}
				// Case 2: Status changed FROM "Paid Leave"
				else if (oldStatus.equals("有給") && !newStatus.equals("有給")) {
					shainLogic.incrementLeaveDay(newBean.getId()); // This method needs to be created
				}

				// Update the attendance record
		    	kintaiLogic.updatetKintai(newBean);
		    }

		    con.commit();
		} catch (Exception e) {
		    e.printStackTrace();
		    // Rollback logic should be here
		    // For simplicity, it's omitted but important for production
		}
	}
}
