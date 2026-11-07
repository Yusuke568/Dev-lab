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
            boolean autoApproved = submitOvertimeRequestUseCase.submit(employeeId, date, plannedStartTime, plannedEndTime, reason);
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
                session.setAttribute("overtimeRequestForm", form);
            }
        }

        return new View("/overtimeRequestMenu.do", true);
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
