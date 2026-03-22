package com.example.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * すべてのアクションクラスが実装するインターフェース。
 * フロントコントローラーからのリクエストを処理する責務を持つ。
 */
public interface Action {

    /**
     * アクションを実行します。
     *
     * @param request  HTTPリクエスト
     * @param response HTTPレスポンス
     * @return 次の遷移先情報を持つViewオブジェクト
     * @throws ServletException Servlet例外
     * @throws IOException      IO例外
     */
    View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;
}
