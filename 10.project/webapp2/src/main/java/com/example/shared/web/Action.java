package com.example.shared.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * すべてのアクションクラスが実裁E��るインターフェース、E
 * フロントコントローラーからのリクエストを処琁E��る責務を持つ、E
 */
public interface Action {

    /**
     * アクションを実行します、E
     *
     * @param request  HTTPリクエスチE
     * @param response HTTPレスポンス
     * @return 次の遷移先情報を持つViewオブジェクチE
     * @throws ServletException Servlet例夁E
     * @throws IOException      IO例夁E
     */
    View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;
}
