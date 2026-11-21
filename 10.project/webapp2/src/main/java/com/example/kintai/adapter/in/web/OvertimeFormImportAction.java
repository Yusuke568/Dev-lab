package com.example.kintai.adapter.in.web;

import com.example.kintai.adapter.out.export.OvertimeFormXlsxImportAdapter;
import com.example.kintai.application.port.in.AttendanceAccessUseCase;
import com.example.kintai.application.service.OvertimeFormImportService;
import com.example.kintai.application.service.OvertimeFormImportService.RowResult;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.service.StaffNameMatcher;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 時間外申請書（Excel）の承認日を勤怠システムに取り込む画面（/overtimeFormImport.do、APPROVAL 権限）。
 *
 *   1. ファイルを複数選んで「読み込む」（op=upload）… 承認欄を読み、氏名欄・ファイル名から社員を選び、
 *      取り込むと何が変わるかを日ごとに出す。まだ保存しない
 *   2. 社員を選び直す（op=staff）… その社員で照らし合わせ直す
 *   3. 「取り込む」（op=import）… 承認日を記録する
 *   op=clear で読み込んだファイルを捨てる。
 * 取り込めるのは、部下の勤怠表に出る社員（組織図で自分の配下と自分）だけ。
 */
public class OvertimeFormImportAction implements Action {

    static final String PENDING = "overtimeFormImportPending";
    private static final String MESSAGES = "overtimeFormImportMessages";
    private static final String ERRORS = "overtimeFormImportErrors";
    private static final int MAX_FILES = 50;

    private final OvertimeFormImportService importService;
    private final AttendanceAccessUseCase attendanceAccessUseCase;
    private final OvertimeFormXlsxImportAdapter parser = new OvertimeFormXlsxImportAdapter();

    public OvertimeFormImportAction(OvertimeFormImportService importService, AttendanceAccessUseCase attendanceAccessUseCase) {
        this.importService = importService;
        this.attendanceAccessUseCase = attendanceAccessUseCase;
    }

    /** 読み込んだファイル1件（「取り込む」までセッションに置く） */
    public static class PendingForm implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String token;
        private final String fileName;
        private final OvertimeFormApprovals form;
        private final String error;
        private String staffId;
        private List<RowResult> preview = List.of();

        PendingForm(String token, String fileName, OvertimeFormApprovals form, String error) {
            this.token = token;
            this.fileName = fileName;
            this.form = form;
            this.error = error;
        }

        public String getToken() { return token; }
        public String getFileName() { return fileName; }
        public String getError() { return error; }
        public String getStaffId() { return staffId; }
        public String getStaffName() { return form == null ? "" : form.staffName(); }
        public List<RowResult> getPreview() { return preview; }
        public long getImportCount() { return preview.stream().filter(r -> !r.actions().isEmpty()).count(); }
        public long getWarnCount() { return preview.stream().filter(r -> !r.notes().isEmpty()).count(); }
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            return new View("/login.do", true);
        }
        List<Subordinate> targets = attendanceAccessUseCase.getSubordinates(loginUser, features(session));
        Map<String, Subordinate> byId = targets.stream()
                .collect(Collectors.toMap(Subordinate::getStaffId, Function.identity(), (a, b) -> a));

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String op = request.getParameter("op");
            if ("upload".equals(op)) {
                upload(request, session, targets, byId);
            } else if ("staff".equals(op)) {
                changeStaff(request, session, byId);
            } else if ("import".equals(op)) {
                importAll(session, byId);
            } else {
                session.removeAttribute(PENDING);
            }
            return new View("/overtimeFormImport.do", true);
        }

        move(session, request, MESSAGES, "messages");
        move(session, request, ERRORS, "errors");
        request.setAttribute("targets", targets);
        request.setAttribute("pending", pending(session));
        request.setAttribute("columnNames", OvertimeFormApprovals.COLUMN_NAMES);
        return new View("/WEB-INF/view/overtime_form_import.jsp");
    }

    private void upload(HttpServletRequest request, HttpSession session, List<Subordinate> targets,
                        Map<String, Subordinate> byId) throws IOException, ServletException {
        List<PendingForm> files = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (!"files".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }
            if (files.size() >= MAX_FILES) {
                errors.add("一度に読み込めるのは" + MAX_FILES + "ファイルまでです。残りはもう一度選んでください。");
                break;
            }
            String fileName = part.getSubmittedFileName() != null ? part.getSubmittedFileName() : "（名前なし）";
            byte[] content;
            try (InputStream in = part.getInputStream()) {
                content = in.readAllBytes();
            }
            String token = UUID.randomUUID().toString();
            try {
                OvertimeFormApprovals form = parser.parse(new ByteArrayInputStream(content));
                PendingForm file = new PendingForm(token, fileName, form, null);
                file.staffId = StaffNameMatcher.match(form.staffName(), fileName, targets).orElse(null);
                refresh(file, byId);
                files.add(file);
            } catch (RuntimeException e) {
                files.add(new PendingForm(token, fileName, null, "読み込めませんでした（" + rootMessage(e) + "）"));
            }
        }
        if (files.isEmpty() && errors.isEmpty()) {
            errors.add("取り込む時間外申請書（.xlsx）を選んでください。");
        }
        session.setAttribute(PENDING, files);
        if (!errors.isEmpty()) {
            session.setAttribute(ERRORS, errors);
        }
    }

    private void changeStaff(HttpServletRequest request, HttpSession session, Map<String, Subordinate> byId) {
        String token = request.getParameter("token");
        for (PendingForm file : pending(session)) {
            if (file.token.equals(token) && file.form != null) {
                String staffId = request.getParameter("staff_" + token);
                file.staffId = staffId == null || staffId.isBlank() ? null : staffId;
                refresh(file, byId);
            }
        }
        session.setAttribute(PENDING, new ArrayList<>(pending(session))); // セッションの変更を知らせる
    }

    /** 選んだ社員で、取り込むと何が変わるかを出し直す */
    private void refresh(PendingForm file, Map<String, Subordinate> byId) {
        file.preview = file.staffId != null && byId.containsKey(file.staffId)
                ? importService.preview(file.staffId, file.form) : List.of();
    }

    private void importAll(HttpSession session, Map<String, Subordinate> byId) {
        List<String> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (PendingForm file : pending(session)) {
            if (file.form == null) {
                continue;
            }
            if (file.staffId == null) {
                messages.add(file.getFileName() + "：社員を選ばなかったので取り込みませんでした。");
                continue;
            }
            Subordinate staff = byId.get(file.staffId);
            if (staff == null) {
                errors.add(file.getFileName() + "：取り込めない社員です（部下の勤怠表に出る社員だけ取り込めます）。");
                continue;
            }
            try {
                List<RowResult> results = importService.apply(file.staffId, file.form);
                long done = results.stream().filter(r -> !r.actions().isEmpty()).count();
                messages.add(file.getFileName() + " → " + staff.getName() + "：" + done + "日分の承認日を取り込みました。");
                List<String> warns = results.stream().filter(r -> !r.notes().isEmpty())
                        .map(r -> r.getDateLabel() + " " + r.getNotesText()).collect(Collectors.toList());
                if (!warns.isEmpty()) {
                    errors.add(file.getFileName() + "：取り込めなかった日があります（"
                            + String.join(" / ", warns.subList(0, Math.min(5, warns.size())))
                            + (warns.size() > 5 ? " ほか" + (warns.size() - 5) + "日" : "") + "）。");
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                errors.add(file.getFileName() + "：取り込みに失敗しました（" + rootMessage(e) + "）。");
            }
        }
        session.removeAttribute(PENDING);
        session.setAttribute(MESSAGES, messages);
        if (!errors.isEmpty()) {
            session.setAttribute(ERRORS, errors);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<PendingForm> pending(HttpSession session) {
        Object value = session.getAttribute(PENDING);
        return value instanceof List ? (List<PendingForm>) value : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Set<String> features(HttpSession session) {
        Object value = session.getAttribute("features");
        return value instanceof Set ? (Set<String>) value : Collections.emptySet();
    }

    private static void move(HttpSession session, HttpServletRequest request, String key, String attr) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(attr, value);
            session.removeAttribute(key);
        }
    }

    private static String rootMessage(Throwable e) {
        return com.example.shared.error.UserMessages.of(e, "ファイルの形式を確かめてください");
    }
}
