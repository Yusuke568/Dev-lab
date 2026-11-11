package com.example.kintai.adapter.in.web;

import com.example.approval.application.service.ApprovalWorkflowService;
import com.example.approval.domain.model.RequestType;
import com.example.kintai.application.port.in.GetPendingApprovalsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.attendance.PendingApprovalItem;
import com.example.leave.application.port.in.LeaveRequestUseCase;
import com.example.leave.domain.model.LeaveRequest;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ログインした承認者宛ての承認待ちを表示するアクション。
 * 残業事前申請・休暇申請は承認ルート上で自分の段になっているもの、
 * 勤怠は組織図上の配下の社員のものだけを表示する。
 */
public class ApprovalListAction implements Action {

    private final GetPendingApprovalsUseCase getPendingApprovalsUseCase;
    private final GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase;
    private final LeaveRequestUseCase leaveRequestUseCase;
    private final ApprovalWorkflowService approvalWorkflowService;
    /** 残業事前申請ごとの、申請者のその月の時間外の状況（グラフ用）。null なら出さない */
    private com.example.kintai.application.service.OvertimeStatusService overtimeStatusService;

    public ApprovalListAction withOvertimeStatus(com.example.kintai.application.service.OvertimeStatusService service) {
        this.overtimeStatusService = service;
        return this;
    }

    public ApprovalListAction(GetPendingApprovalsUseCase getPendingApprovalsUseCase,
                               GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase,
                               LeaveRequestUseCase leaveRequestUseCase,
                               ApprovalWorkflowService approvalWorkflowService) {
        this.getPendingApprovalsUseCase = getPendingApprovalsUseCase;
        this.getPendingOvertimeRequestsUseCase = getPendingOvertimeRequestsUseCase;
        this.leaveRequestUseCase = leaveRequestUseCase;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String approverId = (String) session.getAttribute("loginUser");

        List<PendingApprovalItem> pendingItems = getPendingApprovalsUseCase.getPendingApprovals(approverId);
        List<OvertimeRequest> pendingOvertimeRequests = getPendingOvertimeRequestsUseCase.getPending(approverId);
        List<LeaveRequest> pendingLeaveRequests = leaveRequestUseCase.getPending(approverId);

        request.setAttribute("pendingItems", pendingItems);
        request.setAttribute("pendingOvertimeRequests", pendingOvertimeRequests);
        request.setAttribute("pendingLeaveRequests", pendingLeaveRequests);
        request.setAttribute("overtimeRoutes", approvalWorkflowService.getRoutes(RequestType.OVERTIME,
                pendingOvertimeRequests.stream().map(OvertimeRequest::getId).collect(Collectors.toList())));
        request.setAttribute("overtimeStatusJson", overtimeStatusJson(pendingOvertimeRequests));
        request.setAttribute("leaveRoutes", approvalWorkflowService.getRoutes(RequestType.LEAVE,
                pendingLeaveRequests.stream().map(LeaveRequest::getId).collect(Collectors.toList())));

        if (session.getAttribute("approvalErrorMessage") != null) {
            request.setAttribute("errorMessage", session.getAttribute("approvalErrorMessage"));
            session.removeAttribute("approvalErrorMessage");
        }

        return new View("/WEB-INF/view/approval.jsp");
    }

    /** 申請ID → 時間外の状況。グラフを描く JavaScript に渡す（文字は script を閉じないよう escape する） */
    private String overtimeStatusJson(List<OvertimeRequest> requests) {
        java.util.Map<String, Object> byId = new java.util.LinkedHashMap<>();
        if (overtimeStatusService != null) {
            for (OvertimeRequest r : requests) {
                try {
                    com.example.kintai.application.service.OvertimeStatusService.Status s = overtimeStatusService.statusOf(r);
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("name", r.getEmployeeName());
                    m.put("month", s.month());
                    m.put("actual", s.actualMinutes());
                    m.put("approved", s.approvedPlannedMinutes());
                    m.put("pending", s.otherPendingMinutes());
                    m.put("thisRequest", s.thisRequestMinutes());
                    java.util.List<java.util.Map<String, Object>> lines = new java.util.ArrayList<>();
                    for (com.example.kintai.application.service.OvertimeStatusService.Threshold t : s.thresholds()) {
                        java.util.Map<String, Object> l = new java.util.LinkedHashMap<>();
                        l.put("minutes", t.minutes());
                        l.put("label", t.label());
                        l.put("law", t.law());
                        lines.add(l);
                    }
                    m.put("thresholds", lines);
                    // 折れ線グラフ用：年・月、日ごとの実績、日ごとの予定
                    m.put("year", r.getTargetDate().getYear());
                    m.put("monthValue", r.getTargetDate().getMonthValue());
                    m.put("actualByDay", s.actualByDay());
                    java.util.List<java.util.Map<String, Object>> plannedList = new java.util.ArrayList<>();
                    for (com.example.kintai.application.service.OvertimeStatusService.Planned p : s.planned()) {
                        java.util.Map<String, Object> pm = new java.util.LinkedHashMap<>();
                        pm.put("day", p.day());
                        pm.put("minutes", p.minutes());
                        pm.put("kind", p.kind());
                        plannedList.add(pm);
                    }
                    m.put("planned", plannedList);
                    byId.put(String.valueOf(r.getId()), m);
                } catch (RuntimeException e) {
                    e.printStackTrace(); // グラフが出せなくても承認はできるようにする
                }
            }
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(byId)
                    .replace("</", "<\\/").replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
        } catch (Exception e) {
            return "{}";
        }
    }
}
