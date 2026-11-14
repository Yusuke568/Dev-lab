package com.example.kintai.adapter.in.web;

import com.example.kintai.application.service.OvertimeActualCheckService;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 勤怠承認の画面から、残業の実績を確認する（選んだ申請をまとめて確認できる。APPROVAL 権限）。
 */
public class OvertimeActualConfirmAction implements Action {

    private static final Logger LOG = Logger.getLogger(OvertimeActualConfirmAction.class.getName());

    private final OvertimeActualCheckService actualCheckService;

    public OvertimeActualConfirmAction(OvertimeActualCheckService actualCheckService) {
        this.actualCheckService = actualCheckService;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String approverId = session != null ? (String) session.getAttribute("loginUser") : null;
        if (approverId == null) {
            return new View("/login.do", true);
        }
        List<Integer> ids = new ArrayList<>();
        String[] raw = request.getParameterValues("requestId");
        if (raw != null) {
            for (String v : raw) {
                if (v != null && v.matches("\\d+")) ids.add(Integer.valueOf(v));
            }
        }
        if (ids.isEmpty()) {
            session.setAttribute("approvalErrorMessage", "確認する残業の実績を選んでください。");
            return new View("/approvalList.do", true);
        }
        try {
            java.util.Map<Integer, String> comments = new java.util.HashMap<>();
            for (Integer id : ids) {
                String c = request.getParameter("comment_" + id);
                if (c != null && !c.isBlank()) comments.put(id, c);
            }
            List<String> errors = actualCheckService.confirm(approverId, ids, comments);
            int done = ids.size() - errors.size();
            if (!errors.isEmpty()) {
                session.setAttribute("approvalErrorMessage",
                        (done > 0 ? done + "件の実績を確認しました。" : "") + String.join(" ／ ", errors));
            } else {
                session.setAttribute("approvalSuccessMessage", done + "件の残業の実績を確認しました。");
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "残業の実績を確認できませんでした（承認者 " + approverId + "）", e);
            session.setAttribute("approvalErrorMessage", "残業の実績を確認できませんでした: " + e.getMessage());
        }
        return new View("/approvalList.do", true);
    }
}
