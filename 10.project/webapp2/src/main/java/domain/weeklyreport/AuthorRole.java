package domain.weeklyreport;

/**
 * 週報レビュアー権限ロール
 */
public enum AuthorRole {
    /** 直属上司 / 営業担当 */
    SUPERVISOR,
    /** 最終承認者 / 部長・役員 */
    FINAL_APPROVER
}
