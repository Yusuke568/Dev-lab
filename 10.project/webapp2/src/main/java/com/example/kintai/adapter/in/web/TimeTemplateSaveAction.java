package com.example.kintai.adapter.in.web;

import com.example.kintai.domain.port.out.TimeTemplatePort;
import com.example.shared.web.Action;
import com.example.shared.web.JsonApiSupport;
import com.example.shared.web.View;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 勤怠画面の入力ポップアップから、自分の「時刻テンプレート」を保存する JSON API（ATTENDANCE 権限・POST のみ）。
 * <p>
 * 本文は {"start":"08:30","end":"17:30"}。start・end を省くか空にすると登録を消す。
 * 保存するのはログインしている本人の分だけ（代理入力中でも他人の設定は変えない）。
 * 画面に未保存の入力があっても消えないよう、画面の読み込み直しはせず 204 を返す。
 * CSRF のトークンは FrontController が X-CSRF-Token ヘッダーで確かめる。
 */
public class TimeTemplateSaveAction implements Action {

    private static final Pattern HH_MM = Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TimeTemplatePort timeTemplatePort;

    public TimeTemplateSaveAction(TimeTemplatePort timeTemplatePort) {
        this.timeTemplatePort = timeTemplatePort;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ログインし直してください。");
            return null;
        }
        if (!JsonApiSupport.isJsonRequest(request)) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "送信形式が正しくありません。");
            return null;
        }

        String start;
        String end;
        try {
            JsonNode body = MAPPER.readTree(request.getReader());
            start = text(body, "start");
            end = text(body, "end");
        } catch (IOException e) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "送信内容が正しくありません。");
            return null;
        }
        boolean clear = start == null && end == null;
        if (!clear && (start == null || end == null || !HH_MM.matcher(start).matches() || !HH_MM.matcher(end).matches())) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "出勤・退勤の時刻を正しく入れてから登録してください。");
            return null;
        }

        try {
            timeTemplatePort.saveTimeTemplate(loginUser, clear ? null : start, clear ? null : end);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JsonApiSupport.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "時刻テンプレートを保存できませんでした。時間をおいてもう一度お試しください。");
            return null;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return null;
    }

    /** 文字列の項目。無い・null・空なら null */
    private static String text(JsonNode body, String name) {
        if (body == null || !body.hasNonNull(name)) return null;
        String value = body.get(name).asText().trim();
        return value.isEmpty() ? null : value;
    }
}
