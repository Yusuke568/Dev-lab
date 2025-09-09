package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.CalendarBean;
import model.ConnectionBase;
import model.KintaiLogic;

/**
 * Servlet implementation class ShainMenu
 */

@WebServlet("/ShainKintai")
public class ShainKintai extends HttpServlet{

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShainKintai() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		KintaiLogic kintailogic = new KintaiLogic();
		List<CalendarBean> calendarBeanList = null;
		
		
	    // パラメータ取得
	    int id = Integer.parseInt(request.getParameter("id"));
	    int year = Integer.parseInt(request.getParameter("year"));
	    int month = Integer.parseInt(request.getParameter("month"));
	    
		try (Connection con = ConnectionBase.getConnection()) {
			//対象社員の勤怠情報がない場合
			if(kintailogic.isStaffidwork(id) == -1) {
				
				calendarBeanList = kintailogic.generateCalendar(id,year, month);
				
				con.setAutoCommit(false);
			    for (CalendarBean bean : calendarBeanList) {
			        // ここで各 bean に対して処理を書く
						kintailogic.insertKintai(bean);
			    }
			    con.commit();
			}
			//対象社員の勤怠情報がある場合はDbより指定月の情報を取得する。
			else {
				calendarBeanList = kintailogic.getmonthKintai(id, year, month);
			}
			
		} catch (SQLException | NamingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		request.setAttribute("calendarBeanList", calendarBeanList);
		
		request.setAttribute("statusOptions",new String[] {
			    "通常勤務",
			    "有給",
			    "欠勤",
			    "振替",
			    "時短",
			    "その他"
			} );
		// kintai.jspへ転送
		request.getRequestDispatcher("/WEB-INF/view/kintai.jsp").forward(request, response);	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}