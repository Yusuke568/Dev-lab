package domain.weeklyreport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("週報多層レビュー・アラートテスト")
public class WeeklyReportTest {

    @Test
    @DisplayName("本人提出から上司確認、最終承認まで正常にステータス遷移すること")
    void testNormalApprovalFlow() {
        WeeklyReport report = new WeeklyReport(
                "rep_001", "emp_001", "2026-W41",
                null, null, 3, 3, false, ReportStatus.DRAFT, null, null
        );

        // 1. 本人提出
        report.submit("今週の実績", "来週の予定", 3, 4, false);
        assertEquals(ReportStatus.SUBMITTED, report.getStatus());

        // 2. 上司レビュー（管理層専用メモフラグ true）
        report.addSupervisorReview("sup_001", "現場順調、営業フォロー継続", true);
        assertEquals(ReportStatus.REVIEWED_PRIMARY, report.getStatus());
        assertEquals(1, report.getComments().size());
        assertTrue(report.getComments().get(0).isPrivateToMgmt());

        // 3. 最終承認
        report.approveFinal("boss_001", "承認しました");
        assertEquals(ReportStatus.APPROVED_FINAL, report.getStatus());
        assertEquals(2, report.getComments().size());
        assertFalse(report.getComments().get(1).isPrivateToMgmt());
    }

    @Test
    @DisplayName("体調スコア2以下の場合は緊急アラート対象となること")
    void testEmergencyAlertOnLowHealthScore() {
        WeeklyReport report = new WeeklyReport(
                "rep_002", "emp_001", "2026-W41",
                null, null, 3, 3, false, ReportStatus.DRAFT, null, null
        );

        // 体調2（メンタル不調）で提出
        report.submit("進捗遅れ気味", "挽回予定", 4, 2, false);
        assertTrue(report.requiresImmediateAlert());
    }

    @Test
    @DisplayName("上司レビュー前に最終承認を試みるとエラーとなること")
    void testCannotApproveBeforeSupervisorReview() {
        WeeklyReport report = new WeeklyReport(
                "rep_003", "emp_001", "2026-W41",
                null, null, 3, 3, false, ReportStatus.DRAFT, null, null
        );
        report.submit("実績", "予定", 3, 3, false);

        assertThrows(IllegalStateException.class, () -> {
            report.approveFinal("boss_001", "いきなり承認");
        });
    }

    @Test
    @DisplayName("差戻しを行うとREMANDEDステータスとなりコメントが記録されること")
    void testRemandFlow() {
        WeeklyReport report = new WeeklyReport(
                "rep_004", "emp_001", "2026-W41",
                null, null, 3, 3, false, ReportStatus.DRAFT, null, null
        );
        report.submit("実績", "予定", 3, 3, false);
        report.remand("sup_001", AuthorRole.SUPERVISOR, "来週の予定を具体的に記載してください");

        assertEquals(ReportStatus.REMANDED, report.getStatus());
        assertTrue(report.getComments().get(0).getComment().contains("来週の予定を具体的に"));
    }
}
