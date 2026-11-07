package com.example.leave.adapter.in.web;

import com.example.leave.application.port.in.AddLeaveDaysToEmployeesUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 選択した社員に有給を一括付与するアクション。
 */
public class GrantLeaveSelectedAction implements Action {

    private final AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase;

    public GrantLeaveSelectedAction(AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase) {
        this.addLeaveDaysToEmployeesUseCase = addLeaveDaysToEmployeesUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 画面のフォームは selected_ids / days で送る（以前の名前 shainIds / grantDays も受け付ける）
        String[] ids = request.getParameterValues("selected_ids");
        if (ids == null) ids = request.getParameterValues("shainIds");
        String daysStr = request.getParameter("days");
        if (daysStr == null) daysStr = request.getParameter("grantDays");

        if (ids == null || ids.length == 0) {
            PaidLeaveFlash.error(request, "付与する社員にチェックを入れてください。");
        } else if (daysStr == null || daysStr.isBlank()) {
            PaidLeaveFlash.error(request, "付与日数を入れてください。");
        } else {
            try {
                int days = Integer.parseInt(daysStr.trim());
                if (days <= 0) {
                    throw new NumberFormatException();
                }
                List<ShainId> shainIds = Arrays.stream(ids)
                        .map(ShainId::new)
                        .collect(Collectors.toList());
                addLeaveDaysToEmployeesUseCase.addLeaveDaysToEmployees(shainIds, days);
                PaidLeaveFlash.message(request, shainIds.size() + "人に有給休暇を" + days + "日付与しました。");
            } catch (NumberFormatException e) {
                PaidLeaveFlash.error(request, "付与日数は1以上の整数で入れてください。");
            } catch (RuntimeException e) {
                e.printStackTrace();
                PaidLeaveFlash.error(request, "付与できませんでした。（" + e.getMessage() + "）");
            }
        }
        return new View("/paidLeaveAdmin.do", true);
    }
}
