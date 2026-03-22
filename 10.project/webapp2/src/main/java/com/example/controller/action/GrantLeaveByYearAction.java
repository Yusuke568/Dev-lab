package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.service.LeaveService;

/**
 * 勤続年数に応じて一括で有給を付与するアクションクラス。
 */
public class GrantLeaveByYearAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            LeaveService leaveService = new LeaveService();
            leaveService.grantLeaveByYearsOfService();
            
            // 処理成功後、有給管理画面にリダイレクト
            return new View("/paidLeaveAdmin.do", true);

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "有給の一括付与処理中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
