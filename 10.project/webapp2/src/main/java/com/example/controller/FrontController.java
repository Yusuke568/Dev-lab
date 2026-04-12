package com.example.controller;

import com.example.kintai.configuration.DependencyFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * すべてのリクエストを最初に受け取るフロントコントローラー。（リファクタリング後）
 * DependencyFactoryを利用してActionを取得する。
 */
public class FrontController extends HttpServlet {

    private static final String DEPENDENCY_FACTORY = "dependencyFactory";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // アプリケーション起動時に一度だけDependencyFactoryを初期化
        DependencyFactory factory = new DependencyFactory();
        ServletContext context = config.getServletContext();
        context.setAttribute(DEPENDENCY_FACTORY, factory);
    }

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

            // 2. Actionの実行
            View view = action.execute(request, response);

            // 3. Viewへのディスパッチ
            if (view != null) {
                if (view.isRedirect()) {
                    response.sendRedirect(request.getContextPath() + view.getPath());
                } else {
                    RequestDispatcher dispatcher = request.getRequestDispatcher(view.getPath());
                    dispatcher.forward(request, response);
                }
            }

        } catch (Exception e) {
            // 例外をキャッチした場合、500エラーとして処理（ログ出力含む）
            e.printStackTrace(); // 実際にはロギングする
            throw new ServletException("Internal Server Error in FrontController", e);
        }
    }

    /**
     * リクエストURIから対応するActionオブジェクトをDependencyFactory経由で取得する。
     */
    private Action getAction(HttpServletRequest request) {
        // 例: /shainList.do -> shainList
        String path = request.getServletPath();
        String actionName = path.substring(1, path.lastIndexOf(".do"));

        // 例: shainList -> ShainList
        String capitalizedActionName = Character.toUpperCase(actionName.charAt(0)) + actionName.substring(1);
        
        // FactoryからActionインスタンスを取得
        ServletContext context = getServletContext();
        DependencyFactory factory = (DependencyFactory) context.getAttribute(DEPENDENCY_FACTORY);
        
        return factory.getAction(capitalizedActionName);
    }
}
