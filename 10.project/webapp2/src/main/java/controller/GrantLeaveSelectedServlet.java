package controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.ShainLogic;

@WebServlet("/GrantLeaveSelectedServlet")
public class GrantLeaveSelectedServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] selectedIds = request.getParameterValues("selected_ids");
        int days = Integer.parseInt(request.getParameter("days"));

        if (selectedIds != null && selectedIds.length > 0 && days > 0) {
            ShainLogic shainLogic = new ShainLogic();
            try {
                // This method will be created in ShainLogic
                shainLogic.addLeaveDaysToSelected(selectedIds, days);
            } catch (SQLException | NamingException e) {
                e.printStackTrace();
                // TODO: Add proper error handling
            }
        }

        response.sendRedirect(request.getContextPath() + "/PaidLeaveAdmin");
    }
}
