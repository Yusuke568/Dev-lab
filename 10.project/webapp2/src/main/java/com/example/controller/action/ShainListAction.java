package com.example.controller.action;

import com.example.application.port.in.GetShainListUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 社員一覧表示リクエストを処理するアクションクラス。（新アーキテクチャ）
 */
public class ShainListAction implements Action {

    private final GetShainListUseCase getShainListUseCase;

    public ShainListAction(GetShainListUseCase getShainListUseCase) {
        this.getShainListUseCase = getShainListUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // 1. ユースケースを実行して全社員のリストを取得
            List<Shain> shainList = getShainListUseCase.getShainList();

            // 2. リクエストスコープに社員リストを設定
            request.setAttribute("shainList", shainList);

            // 3. 一覧表示JSPへのフォワード情報を返す
            return new View("/WEB-INF/view/shainlist.jsp");

        } catch (Exception e) {
            // エラーハンドリング: エラーページに遷移させるなど。
            // ここではServletExceptionでラップして上位にスローする。
            throw new ServletException("Failed to get shain list.", e);
        }
    }
}
