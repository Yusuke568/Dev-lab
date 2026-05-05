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
 * 社員一覧を表示するアクション。
 */
public class ShainListAction implements Action {

    private final GetShainListUseCase getShainListUseCase;

    public ShainListAction(GetShainListUseCase getShainListUseCase) {
        this.getShainListUseCase = getShainListUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<ShainDto> shainList = getShainListUseCase.getShainList().stream()
                .map(ShainDto::new)
                .collect(Collectors.toList());
        
        request.setAttribute("shainList", shainList);
        return new View("/WEB-INF/view/shainlist.jsp");
    }
}
