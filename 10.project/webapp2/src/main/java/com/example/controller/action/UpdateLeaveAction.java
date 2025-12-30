package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.service.LeaveService;

/**
 * 特定の社員の有給日数を更新するアクションクラス。
 */
public class UpdateLeaveAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            LeaveService leaveService = new LeaveService();
            int id = Integer.parseInt(request.getParameter("id"));
            int days = Integer.parseInt(request.getParameter("days"));
            
            leaveService.updateDays(id, days);
            
            return new View("/paidLeaveAdmin.do", true);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "IDまたは日数の形式が正しくありません。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "有給日数の更新処理中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
