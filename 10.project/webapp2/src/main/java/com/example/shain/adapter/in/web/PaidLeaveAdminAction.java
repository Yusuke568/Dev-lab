package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.GetShainListUseCase;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 有給管理画面表示のアクションクラス。
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
            List<ShainDto> shainList = getShainListUseCase.getShainList().stream()
                    .map(ShainDto::new)
                    .collect(Collectors.toList());
            
            request.setAttribute("shainList", shainList);
            return new View("/WEB-INF/view/paid_leave_admin.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員情報の取得中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
