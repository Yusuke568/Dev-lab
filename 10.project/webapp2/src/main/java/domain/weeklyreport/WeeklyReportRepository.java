package domain.weeklyreport;

import java.util.Optional;

/**
 * 週報リポジトリインターフェース（Output Port）
 */
public interface WeeklyReportRepository {
    void save(WeeklyReport report);
    Optional<WeeklyReport> findByEmployeeAndWeek(String employeeId, String yearWeek);
    Optional<WeeklyReport> findById(String id);
    Optional<WeeklyReport> findLatestBefore(String employeeId, String yearWeek);
}
