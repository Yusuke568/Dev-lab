package com.example.shared.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * すべての応答（画面・CSS・JavaScript・エラー画面）に、ブラウザ向けの安全のためのヘッダーを付ける。
 * <ul>
 *   <li>X-Frame-Options / CSP frame-ancestors … ほかのサイトの iframe に入れさせない（クリックジャッキング対策）</li>
 *   <li>X-Content-Type-Options … 宣言した種類と違う中身として解釈させない</li>
 *   <li>Referrer-Policy … ほかのサイトへ移るとき、URL（社員IDなどを含む）を送らない</li>
 * </ul>
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = {
        javax.servlet.DispatcherType.REQUEST, javax.servlet.DispatcherType.ERROR })
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader("X-Frame-Options", "DENY");
            res.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
            res.setHeader("X-Content-Type-Options", "nosniff");
            res.setHeader("Referrer-Policy", "same-origin");
        }
        chain.doFilter(request, response);
    }
}
