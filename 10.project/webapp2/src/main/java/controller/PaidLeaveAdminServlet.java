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

import beans.ShainBean;
import model.ShainLogic;

@WebServlet("/PaidLeaveAdmin")
public class PaidLeaveAdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ShainLogic shainLogic = new ShainLogic();
        List<ShainBean> shainList = new ArrayList<>();

        try {
            // This method will need to be created in ShainLogic
            shainList = shainLogic.getAllShainWithLeaveInfo(); 
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            // TODO: Add proper error handling
        }

        request.setAttribute("shainList", shainList);
        request.getRequestDispatcher("/WEB-INF/view/paid_leave_admin.jsp").forward(request, response);
    }
}
