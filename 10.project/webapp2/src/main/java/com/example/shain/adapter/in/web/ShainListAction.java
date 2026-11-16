package com.example.shain.adapter.in.web;

import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.auth.adapter.in.web.LoginUnlockExecuteAction;
import com.example.auth.domain.service.LoginAttemptLimiter;
import com.example.shain.application.port.in.GetShainListUseCase;
import com.example.shain.adapter.in.web.dto.ShainDto;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社員一覧を表示するアクション。
 */
public class ShainListAction implements Action {

    private final GetShainListUseCase getShainListUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public ShainListAction(GetShainListUseCase getShainListUseCase, GetAllClassmastersUseCase getAllClassmastersUseCase,
                           LoginAttemptLimiter loginAttemptLimiter) {
        this.getShainListUseCase = getShainListUseCase;
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<ShainDto> shainList = getShainListUseCase.getShainList().stream()
                .map(ShainDto::new)
                .collect(Collectors.toList());

        request.setAttribute("shainList", shainList);
        request.setAttribute("classmasters", getAllClassmastersUseCase.getAllClassmasters());

        // ログインに続けて失敗して止まっているユーザー名・接続元（管理者が解除できる）
        request.setAttribute("loginLocks", loginAttemptLimiter.currentLocks());
        Map<String, String> names = new HashMap<>();
        shainList.forEach(s -> names.put(String.valueOf(s.getId()), s.getName()));
        request.setAttribute("shainNames", names);

        // 解除の結果を1回だけ出す
        HttpSession session = request.getSession(false);
        if (session != null) {
            moveFlash(session, request, LoginUnlockExecuteAction.FLASH_MESSAGE, "message");
            moveFlash(session, request, LoginUnlockExecuteAction.FLASH_ERROR, "errorMessage");
        }
        return new View("/WEB-INF/view/shainlist.jsp");
    }

    private static void moveFlash(HttpSession session, HttpServletRequest request, String key, String attr) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(attr, value);
            session.removeAttribute(key);
        }
    }
}
