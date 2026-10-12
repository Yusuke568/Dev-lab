package usecase.weeklyreport;

import domain.weeklyreport.AuthorRole;
import domain.weeklyreport.WeeklyReport;
import domain.weeklyreport.WeeklyReportRepository;

import java.util.Objects;

/**
 * 週報レビュー・承認ユースケース
 */
public class ReviewWeeklyReportUseCase {
    private final WeeklyReportRepository weeklyReportRepository;

    public ReviewWeeklyReportUseCase(WeeklyReportRepository weeklyReportRepository) {
        this.weeklyReportRepository = Objects.requireNonNull(weeklyReportRepository);
    }

    /**
     * 上司（一次）レビュー
     */
    public WeeklyReport addSupervisorReview(String reportId, String supervisorId,
                                            String comment, boolean isPrivateToMgmt) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("週報が見つかりません: " + reportId));

        report.addSupervisorReview(supervisorId, comment, isPrivateToMgmt);
        weeklyReportRepository.save(report);
        return report;
    }

    /**
     * 最終承認（部長・責任者）
     */
    public WeeklyReport approveFinal(String reportId, String approverId, String comment) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("週報が見つかりません: " + reportId));

        report.approveFinal(approverId, comment);
        weeklyReportRepository.save(report);
        return report;
    }

    /**
     * 差戻し
     */
    public WeeklyReport remand(String reportId, String authorId, AuthorRole role, String reason) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("週報が見つかりません: " + reportId));

        report.remand(authorId, role, reason);
        weeklyReportRepository.save(report);
        return report;
    }

    /**
     * 管理者非公開メモの追加
     */
    public WeeklyReport addManagementPrivateNote(String reportId, String authorId, AuthorRole role, String note) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("週報が見つかりません: " + reportId));

        report.addManagementPrivateNote(authorId, role, note);
        weeklyReportRepository.save(report);
        return report;
    }

    /**
     * 休暇等によるスキップ処理
     */
    public WeeklyReport skipWeek(String reportId, String employeeId, String yearWeek, String reason) {
        WeeklyReport report = weeklyReportRepository.findByEmployeeAndWeek(employeeId, yearWeek)
                .orElseGet(() -> new WeeklyReport(
                        reportId, employeeId, yearWeek,
                        null, null, 3, 3, false, null, null, null
                ));

        report.skip(reason);
        weeklyReportRepository.save(report);
        return report;
    }
}
