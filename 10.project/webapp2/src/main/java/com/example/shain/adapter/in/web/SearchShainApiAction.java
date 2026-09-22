package com.example.shain.adapter.in.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.shain.application.port.in.GetShainListUseCase;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.shared.web.Action;
import com.example.shared.web.View;

/**
 * 社員を非同期で検索し、結果をJSONで返すAPIアクション。
 */
public class SearchShainApiAction implements Action {

    private final GetShainListUseCase getShainListUseCase;

    public SearchShainApiAction(GetShainListUseCase getShainListUseCase) {
        this.getShainListUseCase = getShainListUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String keyword = request.getParameter("keyword");
            if (keyword == null) {
                keyword = "";
            }
            String jobClass = request.getParameter("jobClass");

            final String finalKeyword = keyword.toLowerCase();
            final String finalJobClass = (jobClass == null || jobClass.isBlank()) ? null : jobClass;

            // Serviceを呼び出して社員を検索し、DTOに変換
            List<ShainDto> shainList = getShainListUseCase.getShainList().stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(finalKeyword))
                .filter(s -> finalJobClass == null || finalJobClass.equals(s.getJobClass()))
                .map(ShainDto::new)
                .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writeValueAsString(shainList);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.print(jsonResult);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while searching for employees.");
        }

        return null;
    }
}
