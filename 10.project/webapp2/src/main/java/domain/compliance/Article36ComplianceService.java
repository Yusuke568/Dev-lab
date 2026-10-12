package domain.compliance;

/**
 * 36協定コンプライアンス監視ドメインサービス
 */
public class Article36ComplianceService {

    /**
     * 月間の法定時間外労働時間（分）からアラートレベルを判定する
     *
     * @param legalOvertimeMinutes 法定外時間外労働（分）
     * @return 36協定アラートレベル
     */
    public OvertimeLevel evaluateMonthlyOvertime(int legalOvertimeMinutes) {
        double hours = (double) legalOvertimeMinutes / 60.0;

        if (hours >= 100.0) {
            return OvertimeLevel.VIOLATION_100H;
        } else if (hours >= 80.0) {
            return OvertimeLevel.DANGER_80H;
        } else if (hours > 45.0) {
            return OvertimeLevel.SPECIAL_CLAUSE_ACTIVE;
        } else if (hours >= 35.0) {
            return OvertimeLevel.WARNING_35H;
        } else {
            return OvertimeLevel.NORMAL;
        }
    }

    /**
     * 特別条項の適用可否（年6回まで）をチェック
     *
     * @param appliedCountInYear 当年度に45時間超過を適用した月数
     * @return 適用可能であれば true
     */
    public boolean canApplySpecialClause(int appliedCountInYear) {
        return appliedCountInYear < 6;
    }

    /**
     * 年間累計時間外労働の制限（年720時間）をチェック
     *
     * @param totalYearlyOvertimeHours 当年の累計法定外時間外労働（h）
     * @return 720時間以内であれば true
     */
    public boolean isWithinYearlyLimit(double totalYearlyOvertimeHours) {
        return totalYearlyOvertimeHours <= 720.0;
    }
}
