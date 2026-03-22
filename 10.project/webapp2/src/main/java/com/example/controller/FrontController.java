package com.example.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * すべてのリクエストを最初に受け取るフロントコントローラー。
 * リクエストに応じて適切なActionクラスを呼び出し、処理を委譲する。
 */
public class FrontController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 1. Actionの特定
            Action action = getAction(request);
            if (action == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action not found.");
                return;
            }

            // 2. Actionの実行
            View view = action.execute(request, response);

            // 3. Viewへのディスパッチ
            if (view != null) {
                if (view.isRedirect()) {
                    // リダイレクト
                    response.sendRedirect(request.getContextPath() + view.getPath());
                } else {
                    // フォワード
                    RequestDispatcher dispatcher = request.getRequestDispatcher(view.getPath());
                    dispatcher.forward(request, response);
                }
            }
            // viewがnullの場合は、Action内でレスポンスが完了している（APIなど）とみなす

        } catch (Exception e) {
            throw new ServletException("Request processing failed.", e);
        }
    }

    /**
     * リクエストURIから対応するActionオブジェクトを生成して返す。
     *
     * @param request HTTPリクエスト
     * @return 生成されたActionオブジェクト
     * @throws Exception Actionの特定またはインスタンス化に失敗した場合
     */
    private Action getAction(HttpServletRequest request) throws Exception {
        // 例: /shainList.do -> shainList
        String path = request.getServletPath();
        String actionName = path.substring(1, path.lastIndexOf(".do"));

        // 例: shainList -> ShainList
        String capitalizedActionName = Character.toUpperCase(actionName.charAt(0)) + actionName.substring(1);
        
        // アクション名の末尾に応じてパッケージを決定
        String packageName;
        // 'Api'で終わるアクションはapiパッケージから探す
        if (capitalizedActionName.endsWith("Api")) {
            packageName = "com.example.controller.api.";
        } else {
            packageName = "com.example.controller.action.";
        }
        
        // 完全修飾クラス名を組み立て
        String className = packageName + capitalizedActionName + "Action";

        try {
            // リフレクションでActionクラスをインスタンス化
            Class<?> actionClass = Class.forName(className);
            return (Action) actionClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            // クラスが見つからない場合はnullを返す（404エラーにつながる）
            return null;
        }
    }
}
