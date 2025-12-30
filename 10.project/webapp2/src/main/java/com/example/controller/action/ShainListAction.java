package com.example.controller.action;

import java.io.IOException;
import java.util.List;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;
import com.example.service.ShainService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 社員一覧表示リクエストを処理するアクションクラス。
 */
public class ShainListAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // 1. Serviceのインスタンスを生成
            ShainService shainService = new ShainService();

            // 2. 全社員のリストを取得
            List<Shain> shainList = shainService.getAllShain();

            // 3. リクエストスコープに社員リストを設定
            request.setAttribute("shainList", shainList);

            // 4. 一覧表示JSPへのフォワード情報を返す
            return new View("/WEB-INF/view/shainlist.jsp");

        } catch (Exception e) {
            // エラーハンドリング: エラーページに遷移させるなど。
            // ここではServletExceptionでラップして上位にスローする。
            throw new ServletException("Failed to get shain list.", e);
        }
    }
}
