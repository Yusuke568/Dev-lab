package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.ClassmasterBean;
import beans.ShainBean;
import model.ClassmasterLogic;
import model.ShainLogic;

/**
 * Servlet implementation class ShainUpdate
 */
@WebServlet("/ShainUpdate")
public class ShainUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShainUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//社員Beanを作成
		ShainLogic shainLogic = new ShainLogic();

		
		try {
			//社員Beanを作成
			ShainBean shainBean = shainLogic.getShainBean(Integer.parseInt(request.getParameter("id")));
			
			// 更新社員をセットする
			request.setAttribute("shainBean", shainBean);
			
			//役職リストを作成 
			List<ClassmasterBean> jobList = new ArrayList<>();
			ClassmasterLogic logic = new ClassmasterLogic();
			
			//リストを取得
			jobList =logic.getAllClassmaster();
			// リストをセットする
			request.setAttribute("jobclassList", jobList);
			
			// update.jspへ転送
			request.getRequestDispatcher("/WEB-INF/view/shainupdate.jsp").forward(request, response);
			
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
