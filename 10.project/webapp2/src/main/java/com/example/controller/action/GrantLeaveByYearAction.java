package com.example.controller.action;

import com.example.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 勤続年数に応じて一括で有給を付与するアクションクラス。（新アーキテクチャ）
 */
public class GrantLeaveByYearAction implements Action {

    private final GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase;

    public GrantLeaveByYearAction(GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase) {
        this.grantLeaveByYearsOfServiceUseCase = grantLeaveByYearsOfServiceUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            grantLeaveByYearsOfServiceUseCase.grantLeaveByYearsOfService();
            
            // 処理成功後、有給管理画面にリダイレクト
            return new View("/paidLeaveAdmin.do", true);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "有給の一括付与処理中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
