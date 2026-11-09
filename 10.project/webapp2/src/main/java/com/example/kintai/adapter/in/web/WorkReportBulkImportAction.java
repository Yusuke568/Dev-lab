package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.AttendanceAccessUseCase;
import com.example.kintai.application.port.in.ImportWorkReportUseCase;
import com.example.kintai.application.port.in.ImportWorkReportUseCase.ImportResult;
import com.example.kintai.application.port.in.ImportWorkReportUseCase.Preview;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.model.employee.EmployeeId;
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
 * 週報担当が、所属部署とその下の部署の社員の作業報告書（Excel）をまとめて取り込む画面（/workReportBulkImport.do）。
 *
 *   1. ファイルを複数選んで「読み込む」（op=upload）… 中身を読み、名前欄・ファイル名から社員を自動で選ぶ。まだ保存しない
 *   2. 社員を確かめて「取り込む」（op=import）… ファイルごとに選んだ社員の勤怠・週次作業報告（リーダー所見を含む）に保存する
 *   op=clear で読み込んだファイルを捨てる。
 * 読み込んだファイルは「取り込む」まで セッションに置く。取り込める社員かどうかは、保存の直前にもう一度確かめる。
 */
public class WorkReportBulkImportAction implements Action {

    static final String PENDING = "workReportBulkImportPending";
    private static final String MESSAGES = "workReportBulkImportMessages";
    private static final String ERRORS = "workReportBulkImportErrors";
    private static final int MAX_FILES = 50;

    private final ImportWorkReportUseCase importWorkReportUseCase;
    private final AttendanceAccessUseCase attendanceAccessUseCase;

    public WorkReportBulkImportAction(ImportWorkReportUseCase importWorkReportUseCase,
                                      AttendanceAccessUseCase attendanceAccessUseCase) {
        this.importWorkReportUseCase = importWorkReportUseCase;
        this.attendanceAccessUseCase = attendanceAccessUseCase;
    }

    /** 読み込んだファイル1件（「取り込む」までセッションに置く） */
    public static class PendingFile implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String token;
        private final String fileName;
        private final byte[] content;
        private final Preview preview;
        private final String suggestedStaffId;
        private final String error;

        PendingFile(String token, String fileName, byte[] content, Preview preview, String suggestedStaffId, String error) {
            this.token = token;
            this.fileName = fileName;
            this.content = content;
            this.preview = preview;
            this.suggestedStaffId = suggestedStaffId;
            this.error = error;
        }

        public String getToken() { return token; }
        public String getFileName() { return fileName; }
        public Preview getPreview() { return preview; }
        public String getSuggestedStaffId() { return suggestedStaffId; }
        public String getError() { return error; }
        public String getMonthsLabel() {
            return preview == null || preview.getMonths().isEmpty() ? "（データなし）" : String.join("・", preview.getMonths());
        }
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            return new View("/login.do", true);
        }
        List<Subordinate> targets = attendanceAccessUseCase.getWeeklyReportTargets(loginUser, features(session));

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String op = request.getParameter("op");
            if ("upload".equals(op)) {
                upload(request, session, targets);
            } else if ("import".equals(op)) {
                importAll(request, session, targets);
            } else {
                session.removeAttribute(PENDING);
            }
            return new View("/workReportBulkImport.do", true);
        }

        move(session, request, MESSAGES, "messages");
        move(session, request, ERRORS, "errors");
        request.setAttribute("targets", targets);
        request.setAttribute("pending", pending(session));
        return new View("/WEB-INF/view/work_report_bulk_import.jsp");
    }

    private void upload(HttpServletRequest request, HttpSession session, List<Subordinate> targets)
            throws IOException, ServletException {
        List<PendingFile> files = new ArrayList<>();
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
                Preview preview = importWorkReportUseCase.preview(content);
                String suggested = StaffNameMatcher.match(preview.getStaffName(), fileName, targets).orElse(null);
                files.add(new PendingFile(token, fileName, content, preview, suggested, null));
            } catch (RuntimeException e) {
                files.add(new PendingFile(token, fileName, null, null, null, "読み込めませんでした（" + rootMessage(e) + "）"));
            }
        }
        if (files.isEmpty() && errors.isEmpty()) {
            errors.add("取り込む作業報告書（.xlsm）を選んでください。");
        }
        session.setAttribute(PENDING, files);
        if (!errors.isEmpty()) {
            session.setAttribute(ERRORS, errors);
        }
    }

    private void importAll(HttpServletRequest request, HttpSession session, List<Subordinate> targets) {
        Map<String, Subordinate> byId = targets.stream()
                .collect(Collectors.toMap(Subordinate::getStaffId, Function.identity(), (a, b) -> a));
        List<String> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (PendingFile file : pending(session)) {
            if (file.content == null) {
                continue;
            }
            String staffId = request.getParameter("staff_" + file.getToken());
            if (staffId == null || staffId.isBlank()) {
                messages.add(file.getFileName() + "：社員を選ばなかったので取り込みませんでした。");
                continue;
            }
            Subordinate staff = byId.get(staffId);
            if (staff == null) {
                errors.add(file.getFileName() + "：取り込めない社員です（所属部署とその下の部署の社員だけ取り込めます）。");
                continue;
            }
            try {
                ImportResult result = importWorkReportUseCase.importFile(new EmployeeId(staffId), new ByteArrayInputStream(file.content));
                String text = file.getFileName() + " → " + staff.getName() + "（ID " + staffId + "）："
                        + result.getImportedMonthCount() + "ヶ月分・勤怠 " + result.getSavedDayCount() + "日・週次作業報告 "
                        + result.getSavedWeekCount() + "週を取り込みました。";
                messages.add(text);
                if (!result.getSkipped().isEmpty()) {
                    errors.add(file.getFileName() + "：" + result.getSkipped().size() + "件をスキップしました（"
                            + result.getSkipped().stream().limit(5).collect(Collectors.joining(" / ")) + "）。");
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
    private static List<PendingFile> pending(HttpSession session) {
        Object value = session.getAttribute(PENDING);
        return value instanceof List ? (List<PendingFile>) value : Collections.emptyList();
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
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
