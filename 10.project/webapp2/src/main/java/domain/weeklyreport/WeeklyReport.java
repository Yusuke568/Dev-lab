package domain.weeklyreport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 週報集約ルートエンティティ
 * （本人・上司・最終承認の3者多層レビュー、SOSアラート判定、不変条件を保護）
 */
public class WeeklyReport {
    private final String id;
    private final String employeeId;
    private final String yearWeek; // 例: "2026-W41"
    private String taskTitle;      // 作業件名、工程名・作業名
    private String taskProgress;   // 作業進捗、作業内容と状況
    private String personalReflection; // 本人所感
    private String progressSummary;
    private String nextWeekPlan;
    private int workloadScore;     // 業務負荷 (1..5)
    private int healthScore;       // 体調・メンタル (1..5)
    private boolean escalationNeeded; // SOSフラグ
    private ReportStatus status;
    private final List<WeeklyReportComment> comments;
    private LocalDateTime submittedAt;

    public WeeklyReport(String id, String employeeId, String yearWeek,
                        String taskTitle, String taskProgress, String personalReflection,
                        String progressSummary, String nextWeekPlan,
                        int workloadScore, int healthScore, boolean escalationNeeded,
                        ReportStatus status, List<WeeklyReportComment> comments,
                        LocalDateTime submittedAt) {
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.employeeId = Objects.requireNonNull(employeeId, "社員IDは必須です。");
        this.yearWeek = Objects.requireNonNull(yearWeek, "週番号は必須です。");
        this.taskTitle = taskTitle;
        this.taskProgress = taskProgress;
        this.personalReflection = personalReflection;
        this.progressSummary = progressSummary;
        this.nextWeekPlan = nextWeekPlan;
        this.workloadScore = (workloadScore >= 1 && workloadScore <= 5) ? workloadScore : 3;
        this.healthScore = (healthScore >= 1 && healthScore <= 5) ? healthScore : 3;
        this.escalationNeeded = escalationNeeded;
        this.status = status != null ? status : ReportStatus.DRAFT;
        this.comments = new ArrayList<>(comments != null ? comments : Collections.emptyList());
        this.submittedAt = submittedAt;
    }

    public WeeklyReport(String id, String employeeId, String yearWeek,
                        String progressSummary, String nextWeekPlan,
                        int workloadScore, int healthScore, boolean escalationNeeded,
                        ReportStatus status, List<WeeklyReportComment> comments,
                        LocalDateTime submittedAt) {
        this(id, employeeId, yearWeek, null, null, null, progressSummary, nextWeekPlan, workloadScore, healthScore, escalationNeeded, status, comments, submittedAt);
    }

    /**
     * 本人による週報提出（3分割実績）
     */
    public void submit(String taskTitle, String taskProgress, String personalReflection) {
        if (this.status != ReportStatus.DRAFT && this.status != ReportStatus.REMANDED) {
            throw new IllegalStateException("下書きまたは差戻し状態以外の週報は提出できません。");
        }
        this.taskTitle = Objects.requireNonNull(taskTitle, "作業件名・工程名は必須です。");
        this.taskProgress = Objects.requireNonNull(taskProgress, "作業進捗・内容は必須です。");
        this.personalReflection = Objects.requireNonNull(personalReflection, "本人所感は必須です。");
        this.progressSummary = String.format("【件名・工程】%s\n【進捗・内容】%s\n【本人所感】%s", taskTitle, taskProgress, personalReflection);
        this.status = ReportStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * 休暇等による週報スキップ
     */
    public void skip(String reason) {
        this.taskTitle = "長期休暇・休業";
        this.taskProgress = reason != null ? reason : "全休のため業務なし";
        this.personalReflection = "提出免除";
        this.progressSummary = "【長期休暇スキップ】 " + (reason != null ? reason : "");
        this.status = ReportStatus.SKIPPED;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * 旧シグネチャ互換提出メソッド
     */
    public void submit(String progressSummary, String nextWeekPlan,
                       int workloadScore, int healthScore, boolean escalationNeeded) {
        if (this.status != ReportStatus.DRAFT && this.status != ReportStatus.REMANDED) {
            throw new IllegalStateException("下書きまたは差戻し状態以外の週報は提出できません。");
        }
        this.progressSummary = Objects.requireNonNull(progressSummary, "業務実績は必須です。");
        this.nextWeekPlan = nextWeekPlan;
        this.workloadScore = (workloadScore >= 1 && workloadScore <= 5) ? workloadScore : 3;
        this.healthScore = (healthScore >= 1 && healthScore <= 5) ? healthScore : 3;
        this.escalationNeeded = escalationNeeded;
        this.status = ReportStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * 上司（一次）レビューの追加
     */
    public void addSupervisorReview(String authorId, String comment, boolean isPrivateToMgmt) {
        if (this.status != ReportStatus.SUBMITTED) {
            throw new IllegalStateException("本人の提出が完了していない週報はレビューできません。");
        }
        this.comments.add(new WeeklyReportComment(
                this.id + "_comm_" + (this.comments.size() + 1),
                authorId, AuthorRole.SUPERVISOR, comment, isPrivateToMgmt, LocalDateTime.now()
        ));
        this.status = ReportStatus.REVIEWED_PRIMARY;
    }

    /**
     * 管理者共有メモの追加（本人非公開・ステータス変動なし）
     */
    public void addManagementPrivateNote(String authorId, AuthorRole role, String note) {
        this.comments.add(new WeeklyReportComment(
                this.id + "_comm_" + (this.comments.size() + 1),
                authorId, role, note, true, LocalDateTime.now()
        ));
    }

    /**
     * 最終承認（部長・責任者）
     */
    public void approveFinal(String approverId, String comment) {
        if (this.status != ReportStatus.REVIEWED_PRIMARY) {
            throw new IllegalStateException("上司レビューが完了していない週報は最終承認できません。");
        }
        if (comment != null && !comment.trim().isEmpty()) {
            this.comments.add(new WeeklyReportComment(
                    this.id + "_comm_" + (this.comments.size() + 1),
                    approverId, AuthorRole.FINAL_APPROVER, comment, false, LocalDateTime.now()
            ));
        }
        this.status = ReportStatus.APPROVED_FINAL;
    }

    /**
     * 差戻し処理
     */
    public void remand(String authorId, AuthorRole role, String reason) {
        if (this.status == ReportStatus.DRAFT || this.status == ReportStatus.APPROVED_FINAL) {
            throw new IllegalStateException("現在のステータスからは差戻しできません。");
        }
        this.comments.add(new WeeklyReportComment(
                this.id + "_comm_" + (this.comments.size() + 1),
                authorId, role, "【差戻し】 " + reason, false, LocalDateTime.now()
        ));
        this.status = ReportStatus.REMANDED;
    }

    /**
     * 緊急アラート判定（体調2以下またはSOS要求）
     */
    public boolean requiresImmediateAlert() {
        return this.escalationNeeded || this.healthScore <= 2;
    }

    public String getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getYearWeek() { return yearWeek; }
    public String getTaskTitle() { return taskTitle; }
    public String getTaskProgress() { return taskProgress; }
    public String getPersonalReflection() { return personalReflection; }
    public String getProgressSummary() { return progressSummary; }
    public String getNextWeekPlan() { return nextWeekPlan; }
    public int getWorkloadScore() { return workloadScore; }
    public int getHealthScore() { return healthScore; }
    public boolean isEscalationNeeded() { return escalationNeeded; }
    public ReportStatus getStatus() { return status; }
    public List<WeeklyReportComment> getComments() { return Collections.unmodifiableList(comments); }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
