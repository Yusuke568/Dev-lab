package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 勤続年数に応じた有給一括付与を実行するアクション。
 */
public class GrantLeaveByYearAction implements Action {

    private final GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase;

    public GrantLeaveByYearAction(GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase) {
        this.grantLeaveByYearsOfServiceUseCase = grantLeaveByYearsOfServiceUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        grantLeaveByYearsOfServiceUseCase.grantLeaveByYearsOfService();
        return new View("/paidLeaveAdmin.do", true);
    }
}
