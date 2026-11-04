package com.example.shain.adapter.in.web;

import com.example.shain.application.port.in.UpdateShainUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 社員更新画面で、入力中の新しい社員IDが使えるかを返すAPI。
 * 返す JSON：{"ok": true} または {"ok": false, "message": "理由"}
 */
public class ShainIdCheckApiAction implements Action {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final UpdateShainUseCase updateShainUseCase;

    public ShainIdCheckApiAction(UpdateShainUseCase updateShainUseCase) {
        this.updateShainUseCase = updateShainUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<String> error = updateShainUseCase.checkNewId(request.getParameter("id"), request.getParameter("newId"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", error.isEmpty());
        error.ifPresent(message -> body.put("message", message));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        MAPPER.writeValue(response.getWriter(), body);
        return null;
    }
}
