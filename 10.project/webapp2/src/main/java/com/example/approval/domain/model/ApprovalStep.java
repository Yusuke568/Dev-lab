package com.example.approval.domain.model;

/**
 * 承認ルートの1段。level は 1 から始まる。
 * decision: null=未処理 / APPROVED / REJECTED
 */
public class ApprovalStep {
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private final int level;
    private final int approverStaffId;
    private final String approverName;
    private final String decision;
    private final String decidedAt; // "yyyy-MM-dd HH:mm" or null
    private final String comment;   // 承認・却下の理由（任意）

    public ApprovalStep(int level, int approverStaffId, String approverName, String decision, String decidedAt) {
        this(level, approverStaffId, approverName, decision, decidedAt, null);
    }

    public ApprovalStep(int level, int approverStaffId, String approverName, String decision, String decidedAt,
                        String comment) {
        this.level = level;
        this.approverStaffId = approverStaffId;
        this.approverName = approverName;
        this.decision = decision;
        this.decidedAt = decidedAt;
        this.comment = comment;
    }

    public int getLevel() { return level; }
    public int getApproverStaffId() { return approverStaffId; }
    public String getApproverName() { return approverName; }
    public String getDecision() { return decision; }
    public String getDecidedAt() { return decidedAt; }
    public String getComment() { return comment; }

    public boolean isDecided() { return decision != null; }

    /** 画面表示用（例: 「1. 山田 承認」）。 */
    public String getLabel() {
        String state = APPROVED.equals(decision) ? "承認" : REJECTED.equals(decision) ? "却下" : "未処理";
        return level + ". " + (approverName != null ? approverName : String.valueOf(approverStaffId)) + " " + state;
    }
}
