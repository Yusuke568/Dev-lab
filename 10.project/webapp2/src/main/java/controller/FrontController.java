package controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * すべてのリクエストを受け付けるフロントコントローラー。
 */
@WebServlet("*.do")
public class FrontController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public FrontController() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 文字コードを設定
        request.setCharacterEncoding("UTF-8");

        // リクエストURIからアクションを特定
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String command = uri.substring(contextPath.length());

        Action action = null;
        String forwardPath = null;

        // URLに応じて実行するActionを決定
        // 将来的にはMapやリフレクションを使って動的に解決するのが望ましい
        if (command.equals("/shain/list.do")) {
            action = new ShainListAction();
        } else if (command.equals("/menu.do")) {
            action = new MenuAction();
        } else if (command.equals("/shain/insert.do")) {
            if ("GET".equals(request.getMethod())) {
                action = new ShainInsertFormAction();
            } else {
                action = new ShainInsertExecuteAction();
            }
        } else if (command.equals("/shain/update.do")) {
            if ("GET".equals(request.getMethod())) {
                action = new ShainUpdateFormAction();
            } else {
                action = new ShainUpdateExecuteAction();
            }
        } else if (command.equals("/shain/delete.do")) {
            if ("GET".equals(request.getMethod())) {
                action = new ShainDeleteFormAction();
            } else {
                action = new ShainDeleteExecuteAction();
            }
        } else if (command.equals("/api/shain/search.do")) {
            action = new SearchShainApiAction();
        } else if (command.equals("/paid_leave/admin.do")) {
            action = new PaidLeaveAdminAction();
        } else if (command.equals("/paid_leave/grant_by_year.do")) {
            if ("POST".equals(request.getMethod())) {
                action = new GrantLeaveByYearAction();
            }
        } else if (command.equals("/paid_leave/update.do")) {
            if ("POST".equals(request.getMethod())) {
                action = new UpdateLeaveAction();
            }
        } else if (command.equals("/paid_leave/grant_selected.do")) {
            if ("POST".equals(request.getMethod())) {
                action = new GrantLeaveSelectedAction();
            }
        } else if (command.equals("/kintai/display.do")) {
            if ("GET".equals(request.getMethod())) {
                action = new KintaiDisplayAction();
            }
        } else if (command.equals("/api/kintai/update.do")) {
            if ("POST".equals(request.getMethod())) {
                action = new KintaiUpdateApiAction();
            }
        } else {
            // 対応するアクションが見つからない場合
            // ここではエラーページに飛ばすなどの処理を実装
            forwardPath = "/WEB-INF/view/error.jsp";
            request.setAttribute("errorMessage", "指定されたページは見つかりません。");
        }

        try {
            if (action != null) {
                forwardPath = action.execute(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "処理中にエラーが発生しました。");
            forwardPath = "/WEB-INF/view/error.jsp";
        }
        
        // 画面遷移
        if (forwardPath != null) {
            // "redirect:" プレフィックスが付いている場合はリダイレクト
            if (forwardPath.startsWith("redirect:")) {
                String redirectPath = contextPath + forwardPath.substring("redirect:".length());
                response.sendRedirect(redirectPath);
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher(forwardPath);
                dispatcher.forward(request, response);
            }
        }
    }
}
