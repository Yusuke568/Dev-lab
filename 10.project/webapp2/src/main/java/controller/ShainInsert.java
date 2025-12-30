package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.ClassmasterBean;
import model.ClassmasterLogic;

/**
 * Servlet implementation class ShainInsert
 */
@WebServlet("/ShainInsert")
public class ShainInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShainInsert() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//入社年リストを作成
		int currentYear = Year.now().getValue();
		List<Integer> yearList = new ArrayList<>();
		for(int year = currentYear;year > currentYear - 150; year--) {
			yearList.add(year);
		}
		//役職リストを作成 
		List<ClassmasterBean> jobList = new ArrayList<>();
		ClassmasterLogic logic = new ClassmasterLogic();
		
		int nextID = 2;
		
		try {
			//リストを取得
			jobList =logic.getAllClassmaster();
			// リストをセットする
			request.setAttribute("jobList", jobList);
			
			request.setAttribute("yearList", yearList);
			request.setAttribute("jobList", jobList);
			request.setAttribute("nextID", nextID);
			
			// insert.jspへ転送
			request.getRequestDispatcher("/WEB-INF/view/shaininsert.jsp").forward(request, response);	
			
		} catch (SQLException | NamingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			//エラーページへ転送
			request.getRequestDispatcher("/WEB-INF/view/error.jsp").forward(request, response);
		}
}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
