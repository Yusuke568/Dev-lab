package com.example.kintai.adapter.in.web;

import com.example.kintai.domain.port.out.DefaultCorrectionPort;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 勤怠画面で、自分の「いつもの補正CD」を保存する（ATTENDANCE 権限）。保存したら同じ月の勤怠画面に戻る。
 */
public class DefaultCorrectionSaveAction implements Action {

    private final DefaultCorrectionPort defaultCorrectionPort;

    public DefaultCorrectionSaveAction(DefaultCorrectionPort defaultCorrectionPort) {
        this.defaultCorrectionPort = defaultCorrectionPort;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            return new View("/login.do", true);
        }
        String value = request.getParameter("correctionId");
        Integer correctionId = value != null && value.matches("\\d+") ? Integer.valueOf(value) : null;
        try {
            defaultCorrectionPort.saveDefaultCorrectionId(loginUser, correctionId);
            session.setAttribute("kintaiSuccessMessage", correctionId == null
                    ? "いつもの補正CDを外しました。"
                    : "いつもの補正CDを保存しました。勤務区分が未設定の日に入力を始めると、この補正CDが入ります。");
        } catch (RuntimeException e) {
            session.setAttribute("kintaiErrorMessage", "いつもの補正CDを保存できませんでした: " + e.getMessage());
        }
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String back = "/kintaiDisplay.do?id=" + loginUser;
        if (year != null && year.matches("\\d{4}") && month != null && month.matches("\\d{1,2}")) {
            back += "&year=" + year + "&month=" + month;
        }
        return new View(back, true);
    }
}
