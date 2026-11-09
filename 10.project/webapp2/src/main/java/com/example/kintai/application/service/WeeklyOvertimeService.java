package com.example.kintai.application.service;

import com.example.application.port.out.WorkTypePort;
import com.example.entity.WorkType;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.CompanyHolidayPort;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.service.JapaneseHolidayCalendar;
import com.example.kintai.domain.service.WeeklyOvertimeCalculator;
import com.example.kintai.domain.service.WeeklyOvertimeCalculator.Day;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 週40時間の時間外（休日出勤の日の分）のための、保存済みの勤怠からの週の組み立て。
 * 計算そのものは WeeklyOvertimeCalculator。
 */
public class WeeklyOvertimeService {

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final CompanyHolidayPort companyHolidayPort;
    private final WorkTypePort workTypePort;

    public WeeklyOvertimeService(LoadAttendanceRecordPort loadAttendanceRecordPort, CompanyHolidayPort companyHolidayPort,
                                 WorkTypePort workTypePort) {
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.companyHolidayPort = companyHolidayPort;
        this.workTypePort = workTypePort;
    }

    /** 勤務区分ごとの有給で所定に数える時間（有給 480、半休 240） */
    public Map<Integer, Integer> leaveCredits() {
        Map<Integer, Integer> credits = new HashMap<>();
        for (WorkType wt : workTypePort.findAll()) {
            credits.put(wt.getId(), (int) Math.round(wt.getPaidLeaveDays() * WeeklyOvertimeCalculator.DAILY_MINUTES));
        }
        return credits;
    }

    /** 休日（土日・祝日・会社休日）か */
    public boolean isHoliday(LocalDate date, Map<LocalDate, String> companyHolidays) {
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY
                || JapaneseHolidayCalendar.isHoliday(date) || companyHolidays.containsKey(date);
    }

    /** 保存済みの勤怠から、その日の1日分を作る（勤怠がなければ勤務なし） */
    public Day toDay(LocalDate date, Optional<AttendanceRecord> record, Map<LocalDate, String> companyHolidays,
                     Map<Integer, Integer> leaveCredits) {
        int total = record.filter(r -> r.getWorkTime() != null)
                .map(r -> (int) r.calculateWorkDuration().toMinutes()).orElse(0);
        int leave = record.map(AttendanceRecord::getAbstractId).map(id -> leaveCredits.getOrDefault(id, 0)).orElse(0);
        return new Day(date, total, isHoliday(date, companyHolidays), leave);
    }

    public Map<LocalDate, String> companyHolidays(LocalDate from, LocalDate to) {
        return companyHolidayPort == null ? Map.of() : companyHolidayPort.findCompanyHolidays(from, to);
    }

    /** 週（日曜〜土曜）の保存済みの勤怠 */
    public List<Day> loadWeek(EmployeeId employeeId, LocalDate weekStart, Map<Integer, Integer> leaveCredits) {
        Map<LocalDate, String> holidays = companyHolidays(weekStart, weekStart.plusDays(6));
        List<Day> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            days.add(toDay(date, loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, date), holidays, leaveCredits));
        }
        return days;
    }

    /**
     * 月の最初と最後の週のうち、その月に入らない日（前月・翌月の日）。
     * 勤怠画面で休日出勤の時間外をその場で計算するときに、表にない日の分として使う。
     */
    public List<Day> outsideMonthDays(EmployeeId employeeId, YearMonth yearMonth) {
        Map<Integer, Integer> credits = leaveCredits();
        List<Day> result = new ArrayList<>();
        LocalDate first = yearMonth.atDay(1);
        LocalDate last = yearMonth.atEndOfMonth();
        Map<LocalDate, String> holidays = companyHolidays(first.minusDays(6), last.plusDays(6));
        for (LocalDate d = WeeklyOvertimeCalculator.weekStart(first); d.isBefore(first); d = d.plusDays(1)) {
            result.add(toDay(d, loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, d), holidays, credits));
        }
        for (LocalDate d = last.plusDays(1); d.getDayOfWeek() != DayOfWeek.SUNDAY; d = d.plusDays(1)) {
            result.add(toDay(d, loadAttendanceRecordPort.loadByEmployeeAndDate(employeeId, d), holidays, credits));
        }
        return result;
    }
}
