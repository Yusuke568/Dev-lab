package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.LocalDate;
import java.util.List;

/**
 * 期間を指定して勤怠記録を読むポート（集計用）。記録のない日の行は作らない。
 */
public interface LoadAttendancePeriodPort {

    /** 開始日〜終了日（両端を含む）の勤怠記録。 */
    List<AttendanceRecord> loadByEmployeeAndPeriod(EmployeeId employeeId, LocalDate from, LocalDate to);
}
