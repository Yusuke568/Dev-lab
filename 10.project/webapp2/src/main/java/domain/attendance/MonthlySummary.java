package domain.attendance;

import domain.contract.SettlementStatus;

import java.util.Objects;

/**
 * 月次勤怠集計エンティティ（集約ルート）
 * （月末締め申請・上司承認・日次ロック連動）
 */
public class MonthlySummary {
    private final String id;
    private final String employeeId;
    private final String yearMonth; // 例: "2026-10"
    private final String assignmentId;
    private int totalWorkMinutes;
    private int directMinutes;
    private int indirectMinutes;
    private int legalOvertimeMinutes;
    private SettlementStatus settlementStatus;
    private MonthlyStatus status;

    public enum MonthlyStatus {
        DRAFT,      // 作成中
        SUBMITTED,  // 確定申請中
        APPROVED,   // 確定承認済（ロック）
        REMANDED    // 差戻し
    }

    public MonthlySummary(String id, String employeeId, String yearMonth, String assignmentId) {
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.employeeId = Objects.requireNonNull(employeeId, "社員IDは必須です。");
        this.yearMonth = Objects.requireNonNull(yearMonth, "年月は必須です。");
        this.assignmentId = Objects.requireNonNull(assignmentId, "アサインIDは必須です。");
        this.status = MonthlyStatus.DRAFT;
        this.settlementStatus = SettlementStatus.WITHIN_RANGE;
    }

    /**
     * 集計値の更新
     */
    public void updateSummaries(int totalWorkMinutes, int directMinutes,
                                int indirectMinutes, int legalOvertimeMinutes,
                                SettlementStatus settlementStatus) {
        if (this.status == MonthlyStatus.APPROVED) {
            throw new IllegalStateException("承認済みの月次データは更新できません。");
        }
        this.totalWorkMinutes = totalWorkMinutes;
        this.directMinutes = directMinutes;
        this.indirectMinutes = indirectMinutes;
        this.legalOvertimeMinutes = legalOvertimeMinutes;
        this.settlementStatus = settlementStatus;
    }

    /**
     * 本人による月次締め確定申請
     */
    public void submit() {
        if (this.status != MonthlyStatus.DRAFT && this.status != MonthlyStatus.REMANDED) {
            throw new IllegalStateException("申請可能なステータスではありません。");
        }
        this.status = MonthlyStatus.SUBMITTED;
    }

    /**
     * 上司による月次確定承認
     */
    public void approve() {
        if (this.status != MonthlyStatus.SUBMITTED) {
            throw new IllegalStateException("提出されていない月次データは承認できません。");
        }
        this.status = MonthlyStatus.APPROVED;
    }

    /**
     * 差戻し
     */
    public void remand() {
        this.status = MonthlyStatus.REMANDED;
    }

    public String getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getYearMonth() { return yearMonth; }
    public String getAssignmentId() { return assignmentId; }
    public int getTotalWorkMinutes() { return totalWorkMinutes; }
    public int getDirectMinutes() { return directMinutes; }
    public int getIndirectMinutes() { return indirectMinutes; }
    public int getLegalOvertimeMinutes() { return legalOvertimeMinutes; }
    public SettlementStatus getSettlementStatus() { return settlementStatus; }
    public MonthlyStatus getStatus() { return status; }
}
