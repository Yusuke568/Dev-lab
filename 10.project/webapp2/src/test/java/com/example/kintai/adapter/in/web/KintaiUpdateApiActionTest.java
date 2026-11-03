package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.application.service.AttendanceAccessService;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.port.out.SubordinatePort;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KintaiUpdateApiActionTest {

    /** 組織図：126 は 127 の承認者（127 は 126 の配下）。128 は配下ではない */
    private static final AttendanceAccessService ACCESS = new AttendanceAccessService(new SubordinatePort() {
        @Override
        public boolean isManagedBy(String employeeId, String approverId) {
            return "126".equals(approverId) && "127".equals(employeeId);
        }

        @Override
        public List<Subordinate> findManagedStaff(String approverId) {
            return List.of();
        }
    });
    private static final Set<String> APPROVER = Set.of("ATTENDANCE", "APPROVAL");
    private static final Set<String> GENERAL = Set.of("ATTENDANCE");

    private final List<List<UpdateAttendanceUseCase.AttendanceUpdateCommand>> calls = new ArrayList<>();
    private final KintaiUpdateApiAction action = new KintaiUpdateApiAction(calls::add, ACCESS);

    private int status = 200;
    private final StringWriter body = new StringWriter();

    private static final String OWN_RECORD = "[{\"id\":126,\"kintaidate\":\"2026-11-03\",\"week\":\"TUE\","
            + "\"kintaifrom\":\"09:00\",\"kintaito\":\"\",\"isTemporary\":true}]";
    private static final String SUBORDINATE_RECORD = "[{\"id\":127,\"kintaidate\":\"2026-11-03\",\"week\":\"TUE\","
            + "\"kintaifrom\":\"09:00\",\"kintaito\":\"18:00\",\"isTemporary\":false}]";
    private static final String OTHERS_RECORD = "[{\"id\":128,\"kintaidate\":\"2026-11-03\",\"week\":\"TUE\","
            + "\"kintaifrom\":\"09:00\",\"kintaito\":\"18:00\",\"isTemporary\":false}]";

    @Test
    void 本人の勤怠は保存できる() throws Exception {
        action.execute(request("126", "application/json", OWN_RECORD), response());

        assertEquals(200, status);
        assertEquals(1, calls.size());
        assertEquals("126", calls.get(0).get(0).getEmployeeId());
    }

    @Test
    void 承認権限があれば配下の社員の勤怠を保存できる() throws Exception {
        action.execute(request("126", APPROVER, "application/json", SUBORDINATE_RECORD), response());

        assertEquals(200, status);
        assertEquals("127", calls.get(0).get(0).getEmployeeId());
    }

    @Test
    void 配下でない社員の勤怠は保存できない() throws Exception {
        action.execute(request("126", APPROVER, "application/json", OTHERS_RECORD), response());

        assertEquals(403, status);
        assertTrue(calls.isEmpty(), "保存処理を呼んでいない");
        assertEquals("この社員の勤怠を保存する権限がありません。", body.toString());
    }

    @Test
    void 承認権限がなければ配下の社員の勤怠も保存できない() throws Exception {
        action.execute(request("126", GENERAL, "application/json", SUBORDINATE_RECORD), response());

        assertEquals(403, status);
        assertTrue(calls.isEmpty());
    }

    @Test
    void 本人と配下でない社員の分が混ざっていたら何も保存しない() throws Exception {
        String mixed = OWN_RECORD.substring(0, OWN_RECORD.length() - 1) + "," + OTHERS_RECORD.substring(1);
        action.execute(request("126", APPROVER, "application/json", mixed), response());

        assertEquals(403, status);
        assertTrue(calls.isEmpty());
    }

    @Test
    void ログインしていなければ保存できない() throws Exception {
        action.execute(request(null, "application/json", OWN_RECORD), response());

        assertEquals(401, status);
        assertTrue(calls.isEmpty());
    }

    @Test
    void JSON以外の送信はフォームからのなりすましの可能性があるので受け付けない() throws Exception {
        action.execute(request("126", "text/plain", OWN_RECORD), response());

        assertEquals(415, status);
        assertTrue(calls.isEmpty());
    }

    @Test
    void 入力内容のエラーは理由を400で返す() throws Exception {
        KintaiUpdateApiAction failing = new KintaiUpdateApiAction(commands -> {
            throw new RuntimeException("トランザクションの実行中にエラーが発生しました",
                    new IllegalArgumentException("退勤時刻が出勤時刻より前になっています: 2026-11-03"));
        }, ACCESS);
        failing.execute(request("126", "application/json", OWN_RECORD), response());

        assertEquals(400, status);
        assertEquals("退勤時刻が出勤時刻より前になっています: 2026-11-03", body.toString());
    }

    // ---- Servlet API の最小限の偽物 ----

    private static HttpServletRequest request(String loginUser, String contentType, String json) {
        return request(loginUser, GENERAL, contentType, json);
    }

    private static HttpServletRequest request(String loginUser, Set<String> features, String contentType, String json) {
        HttpSession session = loginUser == null ? null : (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(), new Class<?>[] {HttpSession.class},
                (proxy, method, args) -> "getAttribute".equals(method.getName())
                        ? Map.<String, Object>of("loginUser", loginUser, "features", features).get(args[0]) : null);
        // 本物と同じく、何度呼んでも同じ reader を返す
        BufferedReader reader = new BufferedReader(new StringReader(json));
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getSession": return session;
                        case "getContentType": return contentType;
                        case "getReader": return reader;
                        case "getMethod": return "POST";
                        default: return null;
                    }
                });
    }

    private HttpServletResponse response() {
        PrintWriter writer = new PrintWriter(body, true);
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setStatus":
                        case "sendError":
                            status = (Integer) args[0];
                            return null;
                        case "getWriter":
                            return writer;
                        default:
                            return null;
                    }
                });
    }
}
