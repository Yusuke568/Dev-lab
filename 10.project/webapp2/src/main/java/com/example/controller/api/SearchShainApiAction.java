package com.example.controller.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.application.port.in.GetShainListUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;

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
            // 検索キーワードを取得
            String keyword = request.getParameter("keyword");
            if (keyword == null) {
                keyword = "";
            }

            final String finalKeyword = keyword.toLowerCase();

            // Serviceを呼び出して社員を検索
            List<Shain> allShain = getShainListUseCase.getShainList();
            List<Shain> shainList = allShain.stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(finalKeyword))
                .collect(Collectors.toList());

            // ObjectMapperを使ってJavaオブジェクトをJSON文字列に変換
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writeValueAsString(shainList);

            // レスポンスのコンテントタイプをJSONに設定
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // レスポンスにJSONを書き込む
            PrintWriter out = response.getWriter();
            out.print(jsonResult);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            // エラーが発生した場合は、HTTPステータス500を返す
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while searching for employees.");
        }

        // 画面遷移は行わないのでnullを返す
        return null;
    }
}
