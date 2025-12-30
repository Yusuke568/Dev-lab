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
 * 選択された社員に有給を付与するアクションクラス。
 */
public class GrantLeaveSelectedAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            LeaveService leaveService = new LeaveService();
            String[] selectedIds = request.getParameterValues("selected_ids");
            String daysStr = request.getParameter("days");
            
            if (selectedIds != null && selectedIds.length > 0 && daysStr != null && !daysStr.isEmpty()) {
                int days = Integer.parseInt(daysStr);
                if (days > 0) {
                    leaveService.addDaysToSelected(selectedIds, days);
                }
            }
            
            return new View("/paidLeaveAdmin.do", true);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "日数の形式が正しくありません。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "有給の付与処理中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
