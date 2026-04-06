package com.example.controller.action;

import com.example.application.port.in.UpdatePaidLeaveDaysUseCase;
import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 特定の社員の有給日数を更新するアクションクラス。（新アーキテクチャ）
 */
public class UpdateLeaveAction implements Action {

    private final UpdatePaidLeaveDaysUseCase updatePaidLeaveDaysUseCase;

    public UpdateLeaveAction(UpdatePaidLeaveDaysUseCase updatePaidLeaveDaysUseCase) {
        this.updatePaidLeaveDaysUseCase = updatePaidLeaveDaysUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            int days = Integer.parseInt(request.getParameter("days"));
            
            updatePaidLeaveDaysUseCase.updatePaidLeaveDays(id, days);
            
            return new View("/paidLeaveAdmin.do", true);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "IDまたは日数の形式が正しくありません。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "有給日数の更新処理中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
