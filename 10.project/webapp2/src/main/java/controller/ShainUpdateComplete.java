package controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.ShainBean;
import model.ShainLogic;

/**
 * Servlet implementation class ShainUpdateComplete
 */
@WebServlet("/ShainUpdateComplete")
public class ShainUpdateComplete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShainUpdateComplete() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//文字コード指定
		request.setCharacterEncoding("UTF-8");
		
		//社員Beanを作成
		ShainLogic shainLogic = new ShainLogic();

		
		try {
			//社員Beanを作成
			ShainBean shainBean = shainLogic.getShainBean(request);
			// DBへ反映
			shainLogic.updateShain(shainBean);
			
			// ShainIndexにリダイレクト
			response.sendRedirect("ShainList");
			
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
