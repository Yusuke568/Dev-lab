package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.AuthenticateUseCase;
import com.example.auth.domain.service.LoginAttemptLimiter;
import com.example.permission.application.port.in.GetUserFeaturesUseCase;
import com.example.permission.domain.model.Feature;
import com.example.shain.application.port.in.GetShainByIdUseCase;
import com.example.shain.domain.model.ShainId;
import com.example.shared.web.Action;
import com.example.shared.web.CsrfToken;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ログイン実行のアクション。
 */
public class LoginExecuteAction implements Action {

    private static final Logger LOG = Logger.getLogger(LoginExecuteAction.class.getName());

    private final AuthenticateUseCase authenticateUseCase;
    private final GetShainByIdUseCase getShainByIdUseCase;
    private final GetUserFeaturesUseCase getUserFeaturesUseCase;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public LoginExecuteAction(AuthenticateUseCase authenticateUseCase, GetShainByIdUseCase getShainByIdUseCase,
                              GetUserFeaturesUseCase getUserFeaturesUseCase, LoginAttemptLimiter loginAttemptLimiter) {
        this.authenticateUseCase = authenticateUseCase;
        this.getShainByIdUseCase = getShainByIdUseCase;
        this.getUserFeaturesUseCase = getUserFeaturesUseCase;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        CsrfToken.preventCaching(response);

        // ログイン画面から送られたものか確かめる（ほかのサイトから、攻撃者のアカウントでログインさせられないように）
        if (!CsrfToken.isValid(request)) {
            CsrfToken.getOrCreate(request.getSession());
            request.setAttribute("errorMessage", "ログイン画面を開いてから時間がたったため、ログインできませんでした。もう一度ログインしてください。");
            return new View("/WEB-INF/view/login.jsp");
        }

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "ユーザー名とパスワードを入力してください。");
            return new View("/WEB-INF/view/login.jsp");
        }

        // 続けて失敗していたら、パスワードを確かめずに断る（止めている間は正しいパスワードでも入れない）
        String address = request.getRemoteAddr();
        Optional<Duration> locked = loginAttemptLimiter.lockedFor(username, address);
        if (locked.isPresent()) {
            request.setAttribute("errorMessage", lockedMessage(locked.get()));
            return new View("/WEB-INF/view/login.jsp");
        }

        if (authenticateUseCase.authenticate(username, password)) {
            loginAttemptLimiter.recordSuccess(username);
            HttpSession session = request.getSession();
            // ログイン前のセッションIDを使い続けさせない（セッション固定攻撃の対策）
            request.changeSessionId();
            session.setAttribute("loginUser", username);
            CsrfToken.getOrCreate(session);
            
            getShainByIdUseCase.getShainById(new ShainId(username)).ifPresent(shain -> {
                session.setAttribute("loginUserRole", shain.getJobClass());
            });

            // 使える機能: features は権限チェック用、can は JSP の表示切り替え用（${sessionScope.can.MASTER_DEPARTMENT}）。
            // can.ANY_MASTER は、どれか1つでもマスタを使えるとき（メニューの「マスタ管理」を出す）
            Set<String> features = new HashSet<>();
            Map<String, Boolean> can = new HashMap<>();
            for (Feature feature : getUserFeaturesUseCase.getFeatures(username)) {
                features.add(feature.name());
                can.put(feature.name(), Boolean.TRUE);
                if (feature.isMaster()) {
                    can.put("ANY_MASTER", Boolean.TRUE);
                }
            }
            session.setAttribute("features", features);
            session.setAttribute("can", can);

            // 管理者が初期化した、または初期パスワードのままなら、変更が終わるまでパスワード変更画面に留める
            if (authenticateUseCase.isPasswordChangeRequired(username)) {
                session.setAttribute("mustChangePassword", Boolean.TRUE);
                return new View("/passwordChange.do", true);
            }
            return new View("/menu.do", true);
        } else {
            if (loginAttemptLimiter.recordFailure(username, address)) {
                LOG.warning("ログインに続けて失敗したため、一時的にログインを止めました（ユーザー名 "
                        + forLog(username) + "、接続元 " + address + "）");
                request.setAttribute("errorMessage", lockedMessage(LoginAttemptLimiter.LOCK));
            } else {
                request.setAttribute("errorMessage", "ユーザー名またはパスワードが間違っています。");
            }
            return new View("/WEB-INF/view/login.jsp");
        }
    }

    static String lockedMessage(Duration remaining) {
        long minutes = Math.max(1, (remaining.getSeconds() + 59) / 60);
        return "ログインに続けて失敗したため、しばらくログインできません。" + minutes + "分ほど待ってから、もう一度お試しください。"
                + "急ぐときは、管理者にロックの解除を依頼してください。";
    }

    /** ログに書くユーザー名から改行などを除き、長さも切る（ログの偽装を防ぐ） */
    private static String forLog(String value) {
        String v = value.replaceAll("\\p{Cntrl}", "?");
        return v.length() > 64 ? v.substring(0, 64) + "…" : v;
    }
}
