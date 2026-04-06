package com.example.controller.action;

import com.example.application.port.in.GetShainListUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 有給管理画面表示のアクションクラス。（新アーキテクチャ）
 */
public class PaidLeaveAdminAction implements Action {

    private final GetShainListUseCase getShainListUseCase;

    public PaidLeaveAdminAction(GetShainListUseCase getShainListUseCase) {
        this.getShainListUseCase = getShainListUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            List<Shain> shainList = getShainListUseCase.getShainList();
            
            request.setAttribute("shainList", shainList);
            return new View("/WEB-INF/view/paid_leave_admin.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員情報の取得中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
