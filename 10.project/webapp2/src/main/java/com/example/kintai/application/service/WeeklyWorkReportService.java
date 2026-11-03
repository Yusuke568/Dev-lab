package com.example.kintai.application.service;

import com.example.kintai.application.port.in.GetWeeklyWorkReportsUseCase;
import com.example.kintai.application.port.in.SaveWeeklyWorkReportsUseCase;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.WeeklyWorkReportPort;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 週次作業報告のユースケース実装。
 */
public class WeeklyWorkReportService implements GetWeeklyWorkReportsUseCase, SaveWeeklyWorkReportsUseCase {

    private final WeeklyWorkReportPort weeklyWorkReportPort;

    public WeeklyWorkReportService(WeeklyWorkReportPort weeklyWorkReportPort) {
        this.weeklyWorkReportPort = weeklyWorkReportPort;
    }

    @Override
    public List<WeeklyWorkReport> getWeeklyWorkReports(EmployeeId employeeId, YearMonth yearMonth) {
        List<LocalDate[]> ranges = WeeklyWorkReport.resolveWeekRanges(yearMonth);
        List<LocalDate> weekEndDates = ranges.stream().map(r -> r[1]).toList();

        Map<LocalDate, WeeklyWorkReport> existingByWeekEnd = new HashMap<>();
        for (WeeklyWorkReport existing : weeklyWorkReportPort.findByEmployeeAndWeekEndDates(employeeId, weekEndDates)) {
            existingByWeekEnd.put(existing.getWeekEndDate(), existing);
        }

        List<WeeklyWorkReport> result = new ArrayList<>();
        for (LocalDate[] range : ranges) {
            LocalDate weekStart = range[0];
            LocalDate weekEnd = range[1];
            WeeklyWorkReport existing = existingByWeekEnd.get(weekEnd);
            if (existing != null) {
                // 表示用の開始日は月の週ブロック構成に基づくものを優先する（第1週が月初で切り詰められるため）
                result.add(new WeeklyWorkReport(employeeId.getValue(), weekStart, weekEnd,
                        existing.getClientName(), existing.getProjectName(), existing.getProcessName(),
                        existing.getProgressStatus(), existing.getProgressDetail(), existing.getPersonalNote(),
                        existing.getProblemCountermeasure()));
            } else {
                result.add(new WeeklyWorkReport(employeeId.getValue(), weekStart, weekEnd,
                        null, null, null, null, null, null));
            }
        }
        return result;
    }

    @Override
    public void saveWeeklyWorkReports(EmployeeId employeeId, List<WeeklyWorkReport> reports) {
        for (WeeklyWorkReport report : reports) {
            weeklyWorkReportPort.upsert(report);
        }
    }
}
