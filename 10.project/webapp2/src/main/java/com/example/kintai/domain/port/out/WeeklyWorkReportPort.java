package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 週次作業報告の永続化操作を定義するポート。
 */
public interface WeeklyWorkReportPort {

    /**
     * 指定された社員の、指定された週終了日（水曜日）群に該当する週次作業報告を取得する。
     * 存在しない週は結果に含まれない。
     */
    List<WeeklyWorkReport> findByEmployeeAndWeekEndDates(EmployeeId employeeId, List<LocalDate> weekEndDates);

    Optional<WeeklyWorkReport> findByEmployeeAndWeekEndDate(EmployeeId employeeId, LocalDate weekEndDate);

    /** 週終了日（staff_id, week_end_date）が既存であれば更新、なければ新規作成する。 */
    void upsert(WeeklyWorkReport report);
}
