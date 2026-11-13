package com.example.kintai.application.service;

import com.example.kintai.application.port.in.GetPendingApprovalsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.domain.port.out.OvertimeActualCheckPort;
import com.example.leave.application.port.in.LeaveRequestUseCase;

import java.util.function.IntSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 承認者の「承認・確認待ち」の件数（メニューの勤怠承認カードと、画面右上のバッジに出す）。
 * 数えられなかったものは 0 にする（バッジが出せなくても画面は開けるように）。
 */
public class ApprovalBadgeService {

    private static final Logger LOG = Logger.getLogger(ApprovalBadgeService.class.getName());

    private final GetPendingOvertimeRequestsUseCase overtimeRequests;
    private final GetPendingApprovalsUseCase attendanceApprovals;
    private final LeaveRequestUseCase leaveRequests;
    private final OvertimeActualCheckPort actualChecks;

    public ApprovalBadgeService(GetPendingOvertimeRequestsUseCase overtimeRequests, GetPendingApprovalsUseCase attendanceApprovals,
                                LeaveRequestUseCase leaveRequests, OvertimeActualCheckPort actualChecks) {
        this.overtimeRequests = overtimeRequests;
        this.attendanceApprovals = attendanceApprovals;
        this.leaveRequests = leaveRequests;
        this.actualChecks = actualChecks;
    }

    /** 承認者の待ちの件数 */
    public Counts countFor(String approverId) {
        int overtime = safe("残業事前申請", () -> overtimeRequests.getPending(approverId).size());
        int actual = actualChecks == null ? 0
                : safe("残業の実績確認", () -> actualChecks.findPendingForApprover(Integer.parseInt(approverId)).size());
        int attendance = safe("勤怠", () -> attendanceApprovals.getPendingApprovals(approverId).size());
        int leave = leaveRequests == null ? 0 : safe("休暇申請", () -> leaveRequests.getPending(approverId).size());
        return new Counts(overtime, actual, attendance, leave);
    }

    private static int safe(String what, IntSupplier counter) {
        try {
            return counter.getAsInt();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, what + "の承認待ちを数えられませんでした", e);
            return 0;
        }
    }

    /** 件数（JSP から読む） */
    public static class Counts {
        private final int overtime;
        private final int actual;
        private final int attendance;
        private final int leave;

        public Counts(int overtime, int actual, int attendance, int leave) {
            this.overtime = overtime;
            this.actual = actual;
            this.attendance = attendance;
            this.leave = leave;
        }

        public int getOvertime() { return overtime; }
        public int getActual() { return actual; }
        public int getAttendance() { return attendance; }
        public int getLeave() { return leave; }
        public int getTotal() { return overtime + actual + attendance + leave; }

        /** 「残業事前申請 2件・残業の実績確認 1件」（0件は出さない） */
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            append(sb, "残業事前申請", overtime);
            append(sb, "残業の実績確認", actual);
            append(sb, "勤怠", attendance);
            append(sb, "休暇申請", leave);
            return sb.toString();
        }

        private static void append(StringBuilder sb, String label, int count) {
            if (count <= 0) return;
            if (sb.length() > 0) sb.append("・");
            sb.append(label).append(' ').append(count).append("件");
        }
    }
}
