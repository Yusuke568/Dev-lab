package domain.contract;

/**
 * 稼働時間精算判定結果値オブジェクト
 */
public class SettlementResult {
    private final double adjustedLowerHours; // 按分調整後 下限時間（h）
    private final double adjustedUpperHours; // 按分調整後 上限時間（h）
    private final double actualDirectHours;   // 実績直接労働時間（h）
    private final SettlementStatus status;     // 判定ステータス
    private final double diffHours;           // 基準外時間（不足時間または超過時間、基準内は0）

    public SettlementResult(double adjustedLowerHours, double adjustedUpperHours,
                            double actualDirectHours, SettlementStatus status, double diffHours) {
        this.adjustedLowerHours = adjustedLowerHours;
        this.adjustedUpperHours = adjustedUpperHours;
        this.actualDirectHours = actualDirectHours;
        this.status = status;
        this.diffHours = diffHours;
    }

    public double getAdjustedLowerHours() { return adjustedLowerHours; }
    public double getAdjustedUpperHours() { return adjustedUpperHours; }
    public double getActualDirectHours() { return actualDirectHours; }
    public SettlementStatus getStatus() { return status; }
    public double getDiffHours() { return diffHours; }
}
