package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.WeeklyWorkSummary;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.YearMonth;
import java.util.List;

/**
 * 週次作業報告の各週の「累計/残業時間」を求めるユースケース。
 */
public interface GetWeeklyWorkSummariesUseCase {

    /** 月の週ブロック（5週）の順に返す。 */
    List<WeeklyWorkSummary> getWeeklyWorkSummaries(EmployeeId employeeId, YearMonth yearMonth);
}
