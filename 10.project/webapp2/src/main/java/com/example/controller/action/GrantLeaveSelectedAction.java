package com.example.controller.action;

import com.example.application.port.in.AddLeaveDaysToEmployeesUseCase;
import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 選択された社員に有給を付与するアクションクラス。（新アーキテクチャ）
 */
public class GrantLeaveSelectedAction implements Action {

    private final AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase;

    public GrantLeaveSelectedAction(AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase) {
        this.addLeaveDaysToEmployeesUseCase = addLeaveDaysToEmployeesUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String[] selectedIds = request.getParameterValues("selected_ids");
            String daysStr = request.getParameter("days");
            
            if (selectedIds != null && selectedIds.length > 0 && daysStr != null && !daysStr.isEmpty()) {
                int days = Integer.parseInt(daysStr);
                addLeaveDaysToEmployeesUseCase.addLeaveDaysToEmployees(selectedIds, days);
            }
            
            return new View("/paidLeaveAdmin.do", true);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "日数の形式が正しくありません。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "有給の付与処理中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
