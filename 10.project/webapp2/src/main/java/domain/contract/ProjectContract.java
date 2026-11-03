package domain.contract;

import java.time.LocalTime;
import java.util.Objects;

/**
 * 案件契約エンティティ
 * （フレームワーク非依存の純粋なドメインモデル）
 */
public class ProjectContract {
    private final String id;
    private final String clientName;
    private final double billingLowerLimitHours; // 下限時間（例: 140.0h）
    private final double billingUpperLimitHours; // 上限時間（例: 180.0h）
    private final LocalTime defaultStartTime;    // 標準始業時刻（例: 09:00）
    private final LocalTime defaultEndTime;      // 標準終業時刻（例: 18:00）
    private final int defaultBreakMinutes;       // 標準休憩時間（例: 60分）

    public ProjectContract(String id, String clientName, double billingLowerLimitHours,
                           double billingUpperLimitHours, LocalTime defaultStartTime,
                           LocalTime defaultEndTime, int defaultBreakMinutes) {
        if (billingLowerLimitHours < 0 || billingUpperLimitHours < billingLowerLimitHours) {
            throw new IllegalArgumentException("下限時間・上限時間の指定が不正です。");
        }
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.clientName = Objects.requireNonNull(clientName, "クライアント名は必須です。");
        this.billingLowerLimitHours = billingLowerLimitHours;
        this.billingUpperLimitHours = billingUpperLimitHours;
        this.defaultStartTime = Objects.requireNonNull(defaultStartTime, "標準始業時刻は必須です。");
        this.defaultEndTime = Objects.requireNonNull(defaultEndTime, "標準終業時刻は必須です。");
        this.defaultBreakMinutes = defaultBreakMinutes;
    }

    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public double getBillingLowerLimitHours() { return billingLowerLimitHours; }
    public double getBillingUpperLimitHours() { return billingUpperLimitHours; }
    public LocalTime getDefaultStartTime() { return defaultStartTime; }
    public LocalTime getDefaultEndTime() { return defaultEndTime; }
    public int getDefaultBreakMinutes() { return defaultBreakMinutes; }
}
