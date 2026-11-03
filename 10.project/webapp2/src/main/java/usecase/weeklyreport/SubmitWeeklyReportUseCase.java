package usecase.weeklyreport;

import domain.attendance.AttendanceRepository;
import domain.attendance.AttendanceTemplateService;
import domain.attendance.DailyAttendance;
import domain.weeklyreport.NotificationGateway;
import domain.weeklyreport.WeeklyReport;
import domain.weeklyreport.WeeklyReportRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 週報提出ユースケース
 * （日次勤怠の未入力チェック＆緊急SOS検知）
 */
public class SubmitWeeklyReportUseCase {
    private final WeeklyReportRepository weeklyReportRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceTemplateService templateService;
    private final NotificationGateway notificationGateway;

    public SubmitWeeklyReportUseCase(WeeklyReportRepository weeklyReportRepository,
                                   AttendanceRepository attendanceRepository,
                                   AttendanceTemplateService templateService,
                                   NotificationGateway notificationGateway) {
        this.weeklyReportRepository = Objects.requireNonNull(weeklyReportRepository);
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository);
        this.templateService = Objects.requireNonNull(templateService);
        this.notificationGateway = notificationGateway;
    }

    public static class Input {
        public final String reportId;
        public final String employeeId;
        public final String yearWeek;
        public final LocalDate submitDate; // 提出日（金曜日など）
        public final String taskTitle;
        public final String taskProgress;
        public final String personalReflection;
        public final String progressSummary;
        public final String nextWeekPlan;
        public final int workloadScore;
        public final int healthScore;
        public final boolean escalationNeeded;

        public Input(String reportId, String employeeId, String yearWeek, LocalDate submitDate,
                     String taskTitle, String taskProgress, String personalReflection) {
            this.reportId = reportId;
            this.employeeId = employeeId;
            this.yearWeek = yearWeek;
            this.submitDate = submitDate;
            this.taskTitle = taskTitle;
            this.taskProgress = taskProgress;
            this.personalReflection = personalReflection;
            this.progressSummary = String.format("【件名・工程】%s\n【進捗・内容】%s\n【本人所感】%s", taskTitle, taskProgress, personalReflection);
            this.nextWeekPlan = null;
            this.workloadScore = 3;
            this.healthScore = 3;
            this.escalationNeeded = false;
        }

        public Input(String reportId, String employeeId, String yearWeek, LocalDate submitDate,
                     String progressSummary, String nextWeekPlan, int workloadScore,
                     int healthScore, boolean escalationNeeded) {
            this.reportId = reportId;
            this.employeeId = employeeId;
            this.yearWeek = yearWeek;
            this.submitDate = submitDate;
            this.taskTitle = null;
            this.taskProgress = null;
            this.personalReflection = null;
            this.progressSummary = progressSummary;
            this.nextWeekPlan = nextWeekPlan;
            this.workloadScore = workloadScore;
            this.healthScore = healthScore;
            this.escalationNeeded = escalationNeeded;
        }
    }

    public WeeklyReport execute(Input input) {
        // 1. 当月の日次勤怠を取得し、提出日以前に未入力日がないかバリデーション（ため込み防止）
        YearMonth ym = YearMonth.from(input.submitDate);
        List<DailyAttendance> attendances = attendanceRepository.findByEmployeeAndMonth(input.employeeId, ym);
        List<DailyAttendance> unfilled = templateService.findUnfilledAttendances(attendances, input.submitDate);
        if (!unfilled.isEmpty()) {
            throw new IllegalStateException(
                    String.format("未入力の日次勤怠が %d 日分存在します。日次勤怠を入力してから週報を提出してください。", unfilled.size())
            );
        }

        // 2. 週報の取得または新規作成
        WeeklyReport report = weeklyReportRepository.findByEmployeeAndWeek(input.employeeId, input.yearWeek)
                .orElseGet(() -> new WeeklyReport(
                        input.reportId, input.employeeId, input.yearWeek,
                        null, null, 3, 3, false, null, null, null
                ));

        // 3. ドメインモデルによる提出処理
        if (input.taskTitle != null) {
            report.submit(input.taskTitle, input.taskProgress, input.personalReflection);
        } else {
            report.submit(
                    input.progressSummary,
                    input.nextWeekPlan,
                    input.workloadScore,
                    input.healthScore,
                    input.escalationNeeded
            );
        }

        weeklyReportRepository.save(report);

        // 4. 緊急SOS・体調不良時の即時アラート通知
        if (report.requiresImmediateAlert() && notificationGateway != null) {
            notificationGateway.notifyEmergencyAlert(report);
        }

        return report;
    }
}
