package com.example.kintai.application.service;

import com.example.kintai.application.port.in.GetWeeklyWorkSummariesUseCase;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.attendance.WeeklyWorkSummary;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendancePeriodPort;
import com.example.kintai.domain.service.WeeklyWorkSummaryCalculator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 週次作業報告の「累計/残業時間」の計算。
 */
public class WeeklyWorkSummaryService implements GetWeeklyWorkSummariesUseCase {

    private final LoadAttendancePeriodPort loadAttendancePeriodPort;

    public WeeklyWorkSummaryService(LoadAttendancePeriodPort loadAttendancePeriodPort) {
        this.loadAttendancePeriodPort = loadAttendancePeriodPort;
    }

    @Override
    public List<WeeklyWorkSummary> getWeeklyWorkSummaries(EmployeeId employeeId, YearMonth yearMonth) {
        List<LocalDate> weekEnds = WeeklyWorkReport.resolveWeekRanges(yearMonth).stream().map(r -> r[1]).toList();
        LocalDate from = yearMonth.minusMonths(1).atDay(1);
        LocalDate lastWeekEnd = weekEnds.get(weekEnds.size() - 1);
        LocalDate to = lastWeekEnd.isAfter(yearMonth.atEndOfMonth()) ? lastWeekEnd : yearMonth.atEndOfMonth();
        List<AttendanceRecord> records = loadAttendancePeriodPort.loadByEmployeeAndPeriod(employeeId, from, to);
        return WeeklyWorkSummaryCalculator.calculate(yearMonth, weekEnds, records);
    }
}
