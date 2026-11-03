package domain.compliance;

/**
 * 有給休暇年5日取得義務トラッキングドメインサービス
 */
public class PaidLeaveComplianceService {

    /**
     * 年5日有給取得義務の進捗アラート判定
     *
     * @param elapsedMonths 付与基準日からの経過月数（0〜12）
     * @param consumedDays 基準日以降の取得日数（全休1.0日、半休0.5日）
     * @return 警告メッセージ（正常時はnull）
     */
    public String checkPaidLeavePace(int elapsedMonths, double consumedDays) {
        if (consumedDays >= 5.0) {
            return null; // 義務達成済み
        }

        // 基準日から半年（6ヶ月）経過時点で2日未満
        if (elapsedMonths >= 6 && elapsedMonths < 9 && consumedDays < 2.0) {
            return String.format("付与後半年が経過しましたが取得日数が %.1f 日です。計画的な取得（年5日義務）を推奨します。", consumedDays);
        }

        // 基準日終了3ヶ月前（9ヶ月経過）時点で4日未満
        if (elapsedMonths >= 9 && consumedDays < 4.0) {
            return String.format("基準期間終了まで3ヶ月を切りましたが取得日数が %.1f 日です。早急に有給取得を申請してください。", consumedDays);
        }

        return null;
    }
}
