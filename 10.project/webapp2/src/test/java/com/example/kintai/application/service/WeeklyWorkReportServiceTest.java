package com.example.kintai.application.service;

import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.WeeklyWorkReportPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 週次作業報告の読み込み（画面に出す分） */
class WeeklyWorkReportServiceTest {

    private final Map<LocalDate, WeeklyWorkReport> saved = new HashMap<>();

    private final WeeklyWorkReportPort port = new WeeklyWorkReportPort() {
        @Override public List<WeeklyWorkReport> findByEmployeeAndWeekEndDates(EmployeeId employeeId, List<LocalDate> weekEndDates) {
            List<WeeklyWorkReport> result = new ArrayList<>();
            weekEndDates.forEach(d -> findByEmployeeAndWeekEndDate(employeeId, d).ifPresent(result::add));
            return result;
        }
        @Override public Optional<WeeklyWorkReport> findByEmployeeAndWeekEndDate(EmployeeId employeeId, LocalDate weekEndDate) {
            return Optional.ofNullable(saved.get(weekEndDate));
        }
        @Override public void upsert(WeeklyWorkReport report) {
            saved.put(report.getWeekEndDate(), report);
        }
    };

    @Test
    void 取り込んだリーダー所見も画面に出す() {
        WeeklyWorkReportService service = new WeeklyWorkReportService(port);
        service.saveWeeklyWorkReports(new EmployeeId("126"), List.of(new WeeklyWorkReport("126",
                LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 4), "(BCC)", null, null, null, null, null, null)
                .replacingLeaderComment("進捗：問題なし\n(椎屋)")));

        WeeklyWorkReport week1 = service.getWeeklyWorkReports(new EmployeeId("126"), YearMonth.of(2026, 11)).get(0);

        assertEquals("(BCC)", week1.getClientName());
        assertEquals("進捗：問題なし\n(椎屋)", week1.getLeaderComment());
    }
}
