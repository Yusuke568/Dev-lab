package com.example.shared.web;

import com.example.shared.bootstrap.DependencyFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * すべてのリクエストを最初に受け取るフロントコントローラー。（�Eキサゴナル移行後！E
 * DependencyFactoryを利用してActionを取得する、E
 */
public class FrontController extends HttpServlet {

    private static final String DEPENDENCY_FACTORY = "dependencyFactory";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // アプリケーション起動時に一度だけDependencyFactoryを�E期化
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
            request.setCharacterEncoding("UTF-8");
            
            // 1. Actionの特宁E
            Action action = getAction(request);

            // 2. Actionの実衁E
            View view = action.execute(request, response);

            // 3. ViewへのチE��スパッチE
            if (view != null) {
                if (view.isRedirect()) {
                    response.sendRedirect(request.getContextPath() + view.getPath());
                } else {
                    RequestDispatcher dispatcher = request.getRequestDispatcher(view.getPath());
                    dispatcher.forward(request, response);
                }
            }

        } catch (Exception e) {
            // 例外をキャチE��した場合、E00エラーとして処琁E��ログ出力含む�E�E
            e.printStackTrace(); // 実際にはロギングする
            throw new ServletException("Internal Server Error in FrontController", e);
        }
    }

    /**
     * リクエスチERIから対応するActionオブジェクトをDependencyFactory経由で取得する、E
     */
    private Action getAction(HttpServletRequest request) {
        // 侁E /shainList.do -> shainList
        String path = request.getServletPath();
        String actionName = path.substring(1, path.lastIndexOf(".do"));

        // 侁E shainList -> ShainList
        String capitalizedActionName = Character.toUpperCase(actionName.charAt(0)) + actionName.substring(1);
        
        // FactoryからActionインスタンスを取征E
        ServletContext context = getServletContext();
        DependencyFactory factory = (DependencyFactory) context.getAttribute(DEPENDENCY_FACTORY);
        
        return factory.getAction(capitalizedActionName);
    }
}
