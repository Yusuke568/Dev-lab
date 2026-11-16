package com.example.shared.web;

import com.example.shared.bootstrap.DependencyFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

import com.example.auth.domain.service.ForcedPasswordChangePolicy;
import com.example.permission.domain.model.Feature;

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

            // 1-2. ログインと権限のチェック
            if (!authorize(request, response)) {
                return;
            }

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
        return getFactory().getAction(getActionName(request));
    }

    /**
     * リクエストURIからアクション名を求める（例: /shainList.do -> ShainList）。
     */
    private String getActionName(HttpServletRequest request) {
        String path = request.getServletPath();
        String actionName = path.substring(1, path.lastIndexOf(".do"));
        return Character.toUpperCase(actionName.charAt(0)) + actionName.substring(1);
    }

    private DependencyFactory getFactory() {
        ServletContext context = getServletContext();
        return (DependencyFactory) context.getAttribute(DEPENDENCY_FACTORY);
    }

    /**
     * ログインと権限を確認する。
     * 未ログインならログイン画面へ、必要な機能の権限がなければエラー画面へ送り、false を返す。
     */
    private boolean authorize(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String actionName = getActionName(request);
        DependencyFactory factory = getFactory();
        if (factory.isPublicAction(actionName)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return false;
        }

        // CSRF 対策：データを変える操作は POST だけにし、POST はすべてセッションのトークンと照合する
        CsrfToken.getOrCreate(session); // 対策を入れる前からログインしていた人にもトークンを用意する
        boolean post = "POST".equalsIgnoreCase(request.getMethod());
        if (!post && factory.isPostOnlyAction(actionName)) {
            forwardError(request, response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "この操作は画面のボタンから行ってください。");
            return false;
        }
        if (post) {
            if (isTooLargeUpload(request)) {
                forwardError(request, response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "ファイルが大きすぎます（1ファイル10MB、合計100MBまで）。");
                return false;
            }
            if (!CsrfToken.isValid(request)) {
                forwardError(request, response, HttpServletResponse.SC_FORBIDDEN,
                        "画面を開いてから時間がたったか、ほかのサイトから送られた操作のため、受け付けませんでした。"
                                + "画面を読み込み直してから、もう一度操作してください。");
                return false;
            }
        }

        // 管理者が初期化した人などは、パスワードを変更するまでほかの画面を使えない（ログアウトは可）
        boolean mustChangePassword = Boolean.TRUE.equals(session.getAttribute("mustChangePassword"));
        if (!ForcedPasswordChangePolicy.isAllowed(actionName, mustChangePassword)) {
            response.sendRedirect(request.getContextPath() + "/passwordChange.do");
            return false;
        }

        Set<Feature> required = factory.getRequiredFeatures(actionName);
        if (required == null) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Set<String> features = (Set<String>) session.getAttribute("features");
        if (features != null && required.stream().anyMatch(f -> features.contains(f.name()))) {
            return true;
        }

        String label = required.size() == 1 ? required.iterator().next().getLabel()
                : required.stream().allMatch(Feature::isMaster) ? Feature.MASTER_GROUP
                : required.stream().map(Feature::getLabel).collect(java.util.stream.Collectors.joining("・"));
        forwardError(request, response, HttpServletResponse.SC_FORBIDDEN, "「" + label + "」を使う権限がありません。");
        return false;
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, int status, String message)
            throws ServletException, IOException {
        response.setStatus(status);
        // JavaScript から JSON を送る保存（勤怠・週次作業報告）には、画面に出せるよう理由を文字で返す
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("application/json")) {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write(message);
            return;
        }
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/WEB-INF/view/error.jsp").forward(request, response);
    }

    /**
     * ファイルのアップロードが大きさの上限を超えているか。
     * 超えているとフォームの項目（トークンも）が読めないので、トークンの照合より先に見分ける。
     */
    private boolean isTooLargeUpload(HttpServletRequest request) throws IOException, ServletException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            return false;
        }
        try {
            request.getParts();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }
}
