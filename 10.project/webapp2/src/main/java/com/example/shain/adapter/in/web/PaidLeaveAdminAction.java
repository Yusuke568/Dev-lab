package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.GetShainListUseCase;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.leave.adapter.in.web.PaidLeaveFlash;
import com.example.leave.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import java.util.stream.Collectors;

/**
 * 有給管理画面表示のアクションクラス。
 */
public class PaidLeaveAdminAction implements Action {

    private final GetShainListUseCase getShainListUseCase;
    /** 一括付与の日数を一覧に出すため。null なら出さない */
    private final GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase;

    public PaidLeaveAdminAction(GetShainListUseCase getShainListUseCase) {
        this(getShainListUseCase, null);
    }

    public PaidLeaveAdminAction(GetShainListUseCase getShainListUseCase,
                                GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase) {
        this.getShainListUseCase = getShainListUseCase;
        this.grantLeaveByYearsOfServiceUseCase = grantLeaveByYearsOfServiceUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PaidLeaveFlash.moveToRequest(request);
        try {
            List<ShainDto> shainList = getShainListUseCase.getShainList().stream()
                    .map(ShainDto::new)
                    .collect(Collectors.toList());

            request.setAttribute("shainList", shainList);
            if (grantLeaveByYearsOfServiceUseCase != null) {
                // 社員ID → 一括付与で足す日数
                Map<Integer, Integer> preview = grantLeaveByYearsOfServiceUseCase.previewGrantDays();
                Map<String, Integer> byId = new HashMap<>();
                preview.forEach((id, days) -> byId.put(String.valueOf(id), days));
                request.setAttribute("grantPreview", byId);
                request.setAttribute("grantPreviewCount", preview.size());
            }
            return new View("/WEB-INF/view/paid_leave_admin.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員情報の取得中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
