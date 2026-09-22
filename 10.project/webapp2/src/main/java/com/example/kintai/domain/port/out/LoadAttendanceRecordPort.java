package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠記録を読み込むためのポ�Eト（インターフェース�E�、E
 *
 * こ�Eインターフェースの実裁E�E、インフラストラクチャ層�E�アダプタ�E�が拁E��します、E
 */
public interface LoadAttendanceRecordPort {

    /**
     * 持E��された社員の特定月の勤怠記録をすべて検索します、E
     *
     * @param employeeId 対象の社員ID
     * @param yearMonth 対象の年朁E
     * @return 勤怠記録のリスチE
     */
    List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId employeeId, YearMonth yearMonth);

    /**
     * 持E��された社員の特定日の勤怠記録を検索します、E
     *
     * @param employeeId 対象の社員ID
     * @param date 対象の日仁E
     * @return 見つかった場合�E勤怠記録のOptional、見つからなぁE��合�EOptional.empty()
     */
    Optional<AttendanceRecord> loadByEmployeeAndDate(EmployeeId employeeId, LocalDate date);

    /**
     * 指定された社員の指定年（暦年）における時間外（残業）分数の合計を返す。
     * 36協定の年間上限チェックに使用する。
     *
     * @param employeeId 対象の社員ID
     * @param year 対象の年（暦年）
     * @return 当該年の時間外分数の合計
     */
    long sumOvertimeMinutesByEmployeeAndYear(EmployeeId employeeId, int year);
}
