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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

        // 複数日をまとめて申請するときは targetDates（"2026-11-20,2026-11-21"）。なければ targetDate の1日
        String targetDates = request.getParameter("targetDates");
        List<String> failedDates = new ArrayList<>();
        try {
            if (employeeId == null) {
                throw new IllegalStateException("ログインしてください。");
            }
            List<LocalDate> dates = parseDates(targetDates, targetDate);
            String departmentParam = request.getParameter("departmentId");
            Integer departmentId = departmentParam == null || departmentParam.isBlank() ? null : Integer.valueOf(departmentParam);
            Integer correction = minutesOf(correctionTime, "補正（通常）");
            Integer correctionMid = minutesOf(correctionMidTime, "補正（深夜）");

            // 日ごとに申請する。申請できなかった日は理由を集め、ほかの日は続ける
            List<LocalDate> submitted = new ArrayList<>();
            int autoApproved = 0;
            List<String> failures = new ArrayList<>();
            for (LocalDate date : dates) {
                try {
                    if (submitOvertimeRequestUseCase.submit(employeeId, date, plannedStartTime, plannedEndTime,
                            reason, departmentId, projectName == null ? "" : projectName,
                            category == null || category.isBlank() ? null : category, correction, correctionMid)) {
                        autoApproved++;
                    }
                    submitted.add(date);
                } catch (RuntimeException e) {
                    if (dates.size() == 1) {
                        throw e;
                    }
                    LOG.log(Level.INFO, "残業事前申請を受け付けられませんでした（社員ID " + employeeId + "、対象日 " + date + "）: " + e.getMessage());
                    failures.add(date.format(SHORT_DATE) + "：" + messageOf(e));
                    failedDates.add(date.toString());
                }
            }
            if (session != null && !submitted.isEmpty()) {
                String days = submitted.size() == 1 ? submitted.get(0) + " の"
                        : submitted.stream().map(d -> d.format(SHORT_DATE)).collect(java.util.stream.Collectors.joining("・"))
                          + " の" + submitted.size() + "件の";
                session.setAttribute("overtimeRequestSuccessMessage", days + "残業事前申請を出しました。"
                        + (autoApproved == submitted.size() ? "組織図で上位の承認者がいないため、自動で承認しました。"
                           : "承認されるまでお待ちください。"));
            }
            if (!failures.isEmpty()) {
                throw new IllegalArgumentException(failures.size() + "日分は申請できませんでした。" + String.join(" ／ ", failures));
            }
        } catch (Exception e) {
            if (failedDates.isEmpty()) {
                LOG.log(Level.WARNING, "残業事前申請を受け付けられませんでした（社員ID " + employeeId + "、対象日 "
                        + (targetDates != null && !targetDates.isBlank() ? targetDates : targetDate) + "）", e);
            }
            if (session != null) {
                session.setAttribute("overtimeRequestErrorMessage", e.getMessage() != null && !failedDates.isEmpty()
                        ? e.getMessage() : messageOf(e));
                Map<String, String> form = new LinkedHashMap<>();
                form.put("targetDate", targetDate);
                // まとめて申請で申請できなかった日だけを、選んだままにして戻す
                form.put("targetDates", !failedDates.isEmpty() ? String.join(",", failedDates) : targetDates);
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

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("M/d");
    /** まとめて申請できる日数 */
    static final int MAX_DATES = 31;

    /** 申請する日（targetDates があればそれ、なければ targetDate の1日）。日付の順・重なりなし */
    static List<LocalDate> parseDates(String targetDates, String targetDate) {
        java.util.TreeSet<LocalDate> dates = new java.util.TreeSet<>();
        String source = targetDates != null && !targetDates.isBlank() ? targetDates : targetDate;
        if (source != null) {
            for (String part : source.split(",")) {
                if (part.isBlank()) continue;
                try {
                    dates.add(LocalDate.parse(part.strip(), DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("対象日が正しくありません: " + part.strip());
                }
            }
        }
        if (dates.isEmpty()) {
            throw new IllegalArgumentException("対象日を入力してください。");
        }
        if (dates.size() > MAX_DATES) {
            throw new IllegalArgumentException("まとめて申請できるのは " + MAX_DATES + " 日までです。");
        }
        return new ArrayList<>(dates);
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
