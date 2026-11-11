package com.example.kintai.application.service;

import com.example.application.port.out.WorkTypePort;
import com.example.entity.WorkType;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.port.out.CompanyHolidayPort;
import com.example.kintai.domain.port.out.SubordinatePort;
import com.example.kintai.domain.service.JapaneseHolidayCalendar;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator;
import com.example.kintai.domain.service.StaffWorkSummaryCalculator.Summary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部下の勤怠表の画面で、部下ごとの当月と年間（1月〜表示する月）のサマリーを出す。
 * 勤怠・事前申請・有給は部下全員分をまとめて読む（読むだけ。空の月の行は作らない）。
 */
public class SubordinateSummaryService {

    private final SubordinatePort subordinatePort;
    private final CompanyHolidayPort companyHolidayPort;
    private final WorkTypePort workTypePort;

    public SubordinateSummaryService(SubordinatePort subordinatePort, CompanyHolidayPort companyHolidayPort,
                                     WorkTypePort workTypePort) {
        this.subordinatePort = subordinatePort;
        this.companyHolidayPort = companyHolidayPort;
        this.workTypePort = workTypePort;
    }

    /** 社員ID → サマリー（部下の並び順） */
    public Map<String, Summary> summaries(List<Subordinate> subordinates, YearMonth month) {
        Map<String, Summary> result = new LinkedHashMap<>();
        if (subordinates.isEmpty()) {
            return result;
        }
        List<String> ids = subordinates.stream().map(Subordinate::getStaffId).collect(Collectors.toList());
        LocalDate yearStart = LocalDate.of(month.getYear(), 1, 1);
        LocalDate end = month.atEndOfMonth();

        Map<Integer, Double> paidLeaveUse = new HashMap<>();
        for (WorkType wt : workTypePort.findAll()) {
            paidLeaveUse.put(wt.getId(), wt.getPaidLeaveDays());
        }
        Map<LocalDate, String> companyHolidays = companyHolidayPort == null ? Map.of()
                : companyHolidayPort.findCompanyHolidays(yearStart, end);
        java.util.function.Predicate<LocalDate> isHoliday = d -> StaffWorkSummaryCalculator.isWeekend(d)
                || JapaneseHolidayCalendar.isHoliday(d) || companyHolidays.containsKey(d);

        var days = subordinatePort.findAttendanceRows(ids, yearStart, end);
        var requests = subordinatePort.findOvertimeRequestRows(ids, month.atDay(1), end);
        var remaining = subordinatePort.findPaidLeaveRemaining(ids);
        for (String id : ids) {
            result.put(id, StaffWorkSummaryCalculator.summarize(month, days.getOrDefault(id, List.of()),
                    requests.getOrDefault(id, List.of()), paidLeaveUse, isHoliday, remaining.get(id)));
        }
        return result;
    }
}
