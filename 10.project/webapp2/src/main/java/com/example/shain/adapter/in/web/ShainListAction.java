package com.example.shain.adapter.in.web;

import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.shain.application.port.in.GetShainListUseCase;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 社員一覧を表示するアクション。
 */
public class ShainListAction implements Action {

    private final GetShainListUseCase getShainListUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;

    public ShainListAction(GetShainListUseCase getShainListUseCase, GetAllClassmastersUseCase getAllClassmastersUseCase) {
        this.getShainListUseCase = getShainListUseCase;
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<ShainDto> shainList = getShainListUseCase.getShainList().stream()
                .map(ShainDto::new)
                .collect(Collectors.toList());

        request.setAttribute("shainList", shainList);
        request.setAttribute("classmasters", getAllClassmastersUseCase.getAllClassmasters());
        return new View("/WEB-INF/view/shainlist.jsp");
    }
}
