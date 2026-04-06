package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠記録を読み込むためのポート（インターフェース）。
 *
 * このインターフェースの実装は、インフラストラクチャ層（アダプタ）が担当します。
 */
public interface LoadAttendanceRecordPort {

    /**
     * 指定された社員の特定月の勤怠記録をすべて検索します。
     *
     * @param employeeId 対象の社員ID
     * @param yearMonth 対象の年月
     * @return 勤怠記録のリスト
     */
    List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId employeeId, YearMonth yearMonth);

    /**
     * 指定された社員の特定日の勤怠記録を検索します。
     *
     * @param employeeId 対象の社員ID
     * @param date 対象の日付
     * @return 見つかった場合は勤怠記録のOptional、見つからない場合はOptional.empty()
     */
    Optional<AttendanceRecord> loadByEmployeeAndDate(EmployeeId employeeId, LocalDate date);
}
