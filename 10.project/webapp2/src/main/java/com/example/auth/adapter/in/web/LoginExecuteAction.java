package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.AuthenticateUseCase;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ログイン実行のアクション。
 */
public class LoginExecuteAction implements Action {

    private final AuthenticateUseCase authenticateUseCase;
    private final GetShainByIdUseCase getShainByIdUseCase;
    private final GetUserFeaturesUseCase getUserFeaturesUseCase;

    public LoginExecuteAction(AuthenticateUseCase authenticateUseCase, GetShainByIdUseCase getShainByIdUseCase,
                              GetUserFeaturesUseCase getUserFeaturesUseCase) {
        this.authenticateUseCase = authenticateUseCase;
        this.getShainByIdUseCase = getShainByIdUseCase;
        this.getUserFeaturesUseCase = getUserFeaturesUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "ユーザー名とパスワードを入力してください。");
            return new View("/WEB-INF/view/login.jsp");
        }

        if (authenticateUseCase.authenticate(username, password)) {
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
            request.setAttribute("errorMessage", "ユーザー名またはパスワードが間違っています。");
            return new View("/WEB-INF/view/login.jsp");
        }
    }
}
