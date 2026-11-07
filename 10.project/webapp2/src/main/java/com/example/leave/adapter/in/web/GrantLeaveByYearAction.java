package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 勤続年数に応じた有給一括付与を実行するアクション。
 * 結果は有給休暇管理画面に1回だけ表示する（PaidLeaveFlash）。
 */
public class GrantLeaveByYearAction implements Action {

    private final GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase;

    public GrantLeaveByYearAction(GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase) {
        this.grantLeaveByYearsOfServiceUseCase = grantLeaveByYearsOfServiceUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new View("/paidLeaveAdmin.do", true);
        }
        try {
            int count = grantLeaveByYearsOfServiceUseCase.grantLeaveByYearsOfService();
            PaidLeaveFlash.message(request, count == 0
                    ? "付与した社員はいません。勤続年数に合う付与ルール（マスタ管理の「有給付与ルール」）があるか確かめてください。"
                    : count + "人に、勤続年数に応じた有給休暇を付与しました。");
        } catch (RuntimeException e) {
            e.printStackTrace();
            PaidLeaveFlash.error(request, "一括付与できませんでした。（" + e.getMessage() + "）");
        }
        return new View("/paidLeaveAdmin.do", true);
    }
}
