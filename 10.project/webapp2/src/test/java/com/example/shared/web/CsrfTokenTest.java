package com.example.shared.web;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTokenTest {

    private final Map<String, Object> sessionAttributes = new HashMap<>();
    private final HttpSession session = (HttpSession) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAttribute":
                        return sessionAttributes.get(args[0]);
                    case "setAttribute":
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    default:
                        return null;
                }
            });

    private HttpServletRequest request(String method, String parameter, String header) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] { HttpServletRequest.class }, (proxy, m, args) -> {
                    switch (m.getName()) {
                        case "getMethod":
                            return method;
                        case "getSession":
                            return session;
                        case "getParameter":
                            return CsrfToken.PARAMETER.equals(args[0]) ? parameter : null;
                        case "getHeader":
                            return CsrfToken.HEADER.equals(args[0]) ? header : null;
                        default:
                            return null;
                    }
                });
    }

    @Test
    void トークンは一度作ったら同じものを返す() {
        String token = CsrfToken.getOrCreate(session);

        assertEquals(token, CsrfToken.getOrCreate(session));
        assertTrue(token.length() >= 40, "32バイトの乱数");
    }

    @Test
    void フォームの項目で送られたトークンが合えば通す() {
        String token = CsrfToken.getOrCreate(session);

        assertTrue(CsrfToken.isValid(request("POST", token, null)));
    }

    @Test
    void ヘッダーで送られたトークンが合えば通す() {
        String token = CsrfToken.getOrCreate(session);

        assertTrue(CsrfToken.isValid(request("POST", null, token)));
    }

    @Test
    void トークンがないか違えば通さない() {
        String token = CsrfToken.getOrCreate(session);

        assertFalse(CsrfToken.isValid(request("POST", null, null)));
        assertFalse(CsrfToken.isValid(request("POST", token + "x", null)));
        assertFalse(CsrfToken.isValid(request("POST", null, "other")));
    }

    @Test
    void GETはトークンが合っても通さない() {
        String token = CsrfToken.getOrCreate(session);

        assertFalse(CsrfToken.isValid(request("GET", token, null)));
    }
}
