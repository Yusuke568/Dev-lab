package domain.weeklyreport;

/**
 * 緊急アラート通知ゲートウェイ（Output Port）
 */
public interface NotificationGateway {
    void notifyEmergencyAlert(WeeklyReport report);
}
