package domain.contract;

/**
 * 稼働時間・精算幅計算ドメインサービス
 * （SES特有の月中按分・精算幅判定・月末着地予測をカプセル化）
 */
public class SettlementService {

    /**
     * 月末確定時の精算判定
     *
     * @param contract 案件契約
     * @param totalBusinessDays 当月総営業日数
     * @param attendedBusinessDays 実参画営業日数（月中入退場考慮）
     * @param actualDirectMinutes 実績直接労働時間（分）
     * @param allocationRatio アサイン比率（例: 1.0）
     * @return 精算判定結果
     */
    public SettlementResult calculate(ProjectContract contract, int totalBusinessDays,
                                     int attendedBusinessDays, int actualDirectMinutes,
                                     double allocationRatio) {
        if (totalBusinessDays <= 0 || attendedBusinessDays <= 0) {
            throw new IllegalArgumentException("営業日数は1日以上である必要があります。");
        }

        // 月中入退場およびアサイン比率に応じた按分率
        double ratio = ((double) attendedBusinessDays / totalBusinessDays) * allocationRatio;
        double adjustedLower = Math.round(contract.getBillingLowerLimitHours() * ratio * 100.0) / 100.0;
        double adjustedUpper = Math.round(contract.getBillingUpperLimitHours() * ratio * 100.0) / 100.0;

        double actualHours = Math.round(((double) actualDirectMinutes / 60.0) * 100.0) / 100.0;

        if (actualHours < adjustedLower) {
            double shortageHours = Math.round((adjustedLower - actualHours) * 100.0) / 100.0;
            return new SettlementResult(adjustedLower, adjustedUpper, actualHours, SettlementStatus.SHORTAGE, shortageHours);
        } else if (actualHours > adjustedUpper) {
            double overtimeHours = Math.round((actualHours - adjustedUpper) * 100.0) / 100.0;
            return new SettlementResult(adjustedLower, adjustedUpper, actualHours, SettlementStatus.OVERTIME, overtimeHours);
        } else {
            return new SettlementResult(adjustedLower, adjustedUpper, actualHours, SettlementStatus.WITHIN_RANGE, 0.0);
        }
    }

    /**
     * 月中着地予測アルゴリズム
     *
     * @param contract 案件契約
     * @param elapsedBusinessDays 経過営業日数
     * @param remainingBusinessDays 残り営業日数
     * @param currentDirectMinutes 現在までの実績直接労働時間（分）
     * @param standardDailyHours 1日の標準所定時間（例: 8.0h）
     * @return 予測着地時間（h）と予測判定
     */
    public SettlementResult predictMonthEnd(ProjectContract contract, int elapsedBusinessDays,
                                           int remainingBusinessDays, int currentDirectMinutes,
                                           double standardDailyHours) {
        if (elapsedBusinessDays <= 0) {
            // 月初（未経過）の場合は標準時間で予測
            double initialEstimatedHours = (elapsedBusinessDays + remainingBusinessDays) * standardDailyHours;
            return evaluateStatus(contract, initialEstimatedHours);
        }

        double currentHours = (double) currentDirectMinutes / 60.0;
        // 実績ペース（1日あたり平均稼働）＋ 残り日数 × 標準稼働での予測
        double pacePerDay = currentHours / elapsedBusinessDays;
        double predictedHours = currentHours + (remainingBusinessDays * pacePerDay);
        predictedHours = Math.round(predictedHours * 100.0) / 100.0;

        return evaluateStatus(contract, predictedHours);
    }

    private SettlementResult evaluateStatus(ProjectContract contract, double predictedHours) {
        double lower = contract.getBillingLowerLimitHours();
        double upper = contract.getBillingUpperLimitHours();

        if (predictedHours < lower) {
            double diff = Math.round((lower - predictedHours) * 100.0) / 100.0;
            return new SettlementResult(lower, upper, predictedHours, SettlementStatus.SHORTAGE, diff);
        } else if (predictedHours > upper) {
            double diff = Math.round((predictedHours - upper) * 100.0) / 100.0;
            return new SettlementResult(lower, upper, predictedHours, SettlementStatus.OVERTIME, diff);
        } else {
            return new SettlementResult(lower, upper, predictedHours, SettlementStatus.WITHIN_RANGE, 0.0);
        }
    }
}
