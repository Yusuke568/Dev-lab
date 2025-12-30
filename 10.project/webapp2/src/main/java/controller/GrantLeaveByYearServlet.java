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

@WebServlet("/GrantLeaveByYearServlet")
public class GrantLeaveByYearServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ShainLogic shainLogic = new ShainLogic();

        try {
            // This method will be created in ShainLogic
            shainLogic.grantLeaveByYearsOfService();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            // TODO: Add proper error handling and feedback to user
        }

        // Redirect back to the admin page
        response.sendRedirect(request.getContextPath() + "/PaidLeaveAdmin");
    }
}
