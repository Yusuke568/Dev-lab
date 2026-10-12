package domain.weeklyreport;

/**
 * 週報ステータス
 */
public enum ReportStatus {
    /** 下書き */
    DRAFT,
    /** 本人提出済 */
    SUBMITTED,
    /** 上司確認済 */
    REVIEWED_PRIMARY,
    /** 最終承認済 */
    APPROVED_FINAL,
    /** 差戻し */
    REMANDED,
    /** 休暇等による提出スキップ */
    SKIPPED
}
