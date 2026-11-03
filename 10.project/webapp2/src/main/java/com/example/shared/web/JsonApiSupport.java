package com.example.shared.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 画面の JavaScript から fetch で呼ぶ JSON API の共通処理。
 */
public final class JsonApiSupport {

    private JsonApiSupport() {
    }

    /**
     * Content-Type が application/json か確かめる。
     * ほかのサイトのフォームからは application/json を送れない（ブラウザが事前確認で止める）ので、
     * フォームを使った CSRF でこの API を呼ばれるのを防げる。
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("application/json");
    }

    /**
     * エラーを、画面にそのまま表示できる日本語のテキストで返す。
     */
    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(message);
    }
}
