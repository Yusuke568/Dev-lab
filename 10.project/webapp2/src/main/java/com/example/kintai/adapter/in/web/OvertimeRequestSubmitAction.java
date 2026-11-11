package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.SubmitOvertimeRequestUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 残業事前申請の新規提出・再提出を行うアクション。
 * 申請できなかったときは、理由と入力内容をセッションに残して申請画面へ戻す（画面で理由を表示し、入力をやり直さなくて済むようにする）。
 */
public class OvertimeRequestSubmitAction implements Action {

    private static final Logger LOG = Logger.getLogger(OvertimeRequestSubmitAction.class.getName());

    private final SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase;

    public OvertimeRequestSubmitAction(SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase) {
        this.submitOvertimeRequestUseCase = submitOvertimeRequestUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;
        String targetDate = request.getParameter("targetDate");
        String plannedStartTime = request.getParameter("plannedStartTime");
        String plannedEndTime = request.getParameter("plannedEndTime");
        String reason = request.getParameter("reason");
        String projectName = request.getParameter("projectName");
        String category = request.getParameter("category");
        String correctionTime = request.getParameter("correctionTime");
        String correctionMidTime = request.getParameter("correctionMidTime");

        try {
            if (employeeId == null) {
                throw new IllegalStateException("ログインしてください。");
            }
            LocalDate date;
            try {
                date = LocalDate.parse(targetDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException | NullPointerException e) {
                throw new IllegalArgumentException("対象日を入力してください。");
            }
            String departmentParam = request.getParameter("departmentId");
            Integer departmentId = departmentParam == null || departmentParam.isBlank() ? null : Integer.valueOf(departmentParam);
            boolean autoApproved = submitOvertimeRequestUseCase.submit(employeeId, date, plannedStartTime, plannedEndTime,
                    reason, departmentId, projectName == null ? "" : projectName,
                    category == null || category.isBlank() ? null : category,
                    minutesOf(correctionTime, "補正（通常）"), minutesOf(correctionMidTime, "補正（深夜）"));
            if (session != null) {
                session.setAttribute("overtimeRequestSuccessMessage", autoApproved
                        ? date + " の残業事前申請を出しました。組織図で上位の承認者がいないため、自動で承認しました。"
                        : date + " の残業事前申請を出しました。承認されるまでお待ちください。");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "残業事前申請を受け付けられませんでした（社員ID " + employeeId + "、対象日 " + targetDate + "）", e);
            if (session != null) {
                session.setAttribute("overtimeRequestErrorMessage", messageOf(e));
                Map<String, String> form = new LinkedHashMap<>();
                form.put("targetDate", targetDate);
                form.put("plannedStartTime", plannedStartTime);
                form.put("plannedEndTime", plannedEndTime);
                form.put("reason", reason);
                form.put("projectName", projectName);
                form.put("category", category);
                form.put("correctionTime", correctionTime);
                form.put("correctionMidTime", correctionMidTime);
                form.put("departmentId", request.getParameter("departmentId"));
                session.setAttribute("overtimeRequestForm", form);
            }
        }

        return new View("/overtimeRequestMenu.do", true);
    }

    /** 「H:mm」「HH:mm」を分に。空なら null（その日の勤怠の補正を使う） */
    static Integer minutesOf(String value, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(value.strip());
        if (!m.matches() || Integer.parseInt(m.group(2)) >= 60) {
            throw new IllegalArgumentException(label + "は「1:30」のように時:分で入れてください。");
        }
        return Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2));
    }

    /**
     * 画面に出す理由。申請の入力や設定の問題（IllegalArgument/IllegalState）はそのメッセージを、
     * DB の障害などは原因をたどって見つけた入力系のメッセージを、なければ定型文を出す（理由が空で何も表示されないことをなくす）。
     */
    private static String messageOf(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if ((t instanceof IllegalArgumentException || t instanceof IllegalStateException)
                    && t.getMessage() != null && !t.getMessage().isBlank()) {
                return t.getMessage();
            }
        }
        return "申請を保存できませんでした。時間をおいてもう一度申請してください。続く場合は管理者に連絡してください。";
    }
}
