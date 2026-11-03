package domain.attendance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠リポジトリインターフェース（Output Port）
 */
public interface AttendanceRepository {
    void save(DailyAttendance attendance);
    void saveAll(List<DailyAttendance> attendances);
    Optional<DailyAttendance> findByEmployeeAndDate(String employeeId, LocalDate date);
    List<DailyAttendance> findByEmployeeAndMonth(String employeeId, YearMonth yearMonth);
    void saveMonthlySummary(MonthlySummary summary);
    Optional<MonthlySummary> findMonthlySummary(String employeeId, YearMonth yearMonth);
    List<AbstractItem> findAllAbstractItems();
}
