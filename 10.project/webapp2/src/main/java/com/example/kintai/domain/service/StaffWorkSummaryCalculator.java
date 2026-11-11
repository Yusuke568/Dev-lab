package com.example.kintai.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 部下のサマリー（当月と年間）の集計。
 *   年間は、その年の1月1日から表示する月の月末まで（年初からの累計）。
 *   時間外の上限は、当月が45時間、年間が540時間（36協定。勤怠画面の年間時間外のチェックと同じ）。
 */
public final class StaffWorkSummaryCalculator {

    public static final int MONTHLY_LIMIT_MINUTES = 45 * 60;
    public static final int YEARLY_LIMIT_MINUTES = 540 * 60;

    private StaffWorkSummaryCalculator() {
    }

    /** 勤怠1日分（worked は出勤・退勤の両方が入っている日） */
    public record DayRow(LocalDate date, boolean worked, int totalMinutes, int overtimeMinutes, Integer abstractId) {
    }

    /** 残業事前申請1件（status は PENDING / APPROVED / REJECTED / WITHDRAWN） */
    public record RequestRow(LocalDate date, String status) {
    }

    /** 期間の集計 */
    public record Totals(int totalMinutes, int overtimeMinutes, int workDays, int holidayWorkDays, double paidLeaveDays) {
    }

    /**
     * 1人分のサマリー。
     * @param overtimeByMonth 1月〜表示する月までの、月ごとの時間外（添字0が1月）
     * @param pendingRequests 当月の申請中の残業事前申請の件数
     * @param approvedRequests 当月の承認済みの残業事前申請の件数
     * @param unappliedDays 当月の、時間外があるのに申請中・承認済みの事前申請がない日数
     */
    public record Summary(Totals month, Totals year, int[] overtimeByMonth, Double paidLeaveRemaining,
                          int pendingRequests, int approvedRequests, int unappliedDays) {

        /** 当月の時間外の、上限（45時間）に対する割合（%） */
        public int monthOvertimePercent() {
            return percent(month.overtimeMinutes(), MONTHLY_LIMIT_MINUTES);
        }

        /** 年間の時間外の、上限（540時間）に対する割合（%） */
        public int yearOvertimePercent() {
            return percent(year.overtimeMinutes(), YEARLY_LIMIT_MINUTES);
        }

        private static int percent(int value, int limit) {
            return (int) Math.round(value * 100.0 / limit);
        }
    }

    /**
     * @param days          その年の1月1日から表示する月の月末までの勤怠
     * @param requests      当月の残業事前申請
     * @param paidLeaveUse  勤務区分ID → 有給を使う日数（有給 1、半休 0.5）
     * @param isHoliday     休日（土日・祝日・会社休日）か
     */
    public static Summary summarize(YearMonth month, List<DayRow> days, List<RequestRow> requests,
                                    Map<Integer, Double> paidLeaveUse, Predicate<LocalDate> isHoliday,
                                    Double paidLeaveRemaining) {
        LocalDate yearStart = LocalDate.of(month.getYear(), 1, 1);
        LocalDate monthStart = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        int[] byMonth = new int[month.getMonthValue()];
        Accumulator m = new Accumulator();
        Accumulator y = new Accumulator();
        Set<LocalDate> overtimeDays = new HashSet<>();
        for (DayRow d : days) {
            if (d.date().isBefore(yearStart) || d.date().isAfter(end)) {
                continue;
            }
            double leave = d.abstractId() != null ? paidLeaveUse.getOrDefault(d.abstractId(), 0.0) : 0.0;
            boolean holidayWork = d.worked() && isHoliday.test(d.date());
            y.add(d, holidayWork, leave);
            byMonth[d.date().getMonthValue() - 1] += Math.max(0, d.overtimeMinutes());
            if (!d.date().isBefore(monthStart)) {
                m.add(d, holidayWork, leave);
                if (d.overtimeMinutes() > 0) {
                    overtimeDays.add(d.date());
                }
            }
        }
        int pending = 0;
        int approved = 0;
        Set<LocalDate> covered = new HashSet<>();
        for (RequestRow r : requests) {
            if (!YearMonth.from(r.date()).equals(month)) {
                continue;
            }
            if ("PENDING".equals(r.status())) {
                pending++;
                covered.add(r.date());
            } else if ("APPROVED".equals(r.status())) {
                approved++;
                covered.add(r.date());
            }
        }
        overtimeDays.removeAll(covered);
        return new Summary(m.totals(), y.totals(), byMonth, paidLeaveRemaining, pending, approved, overtimeDays.size());
    }

    /** 土日か（祝日・会社休日は呼び出し側で足す） */
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static final class Accumulator {
        int total;
        int overtime;
        int workDays;
        int holidayWorkDays;
        double paidLeave;

        void add(DayRow d, boolean holidayWork, double leave) {
            total += Math.max(0, d.totalMinutes());
            overtime += Math.max(0, d.overtimeMinutes());
            if (d.worked()) {
                workDays++;
            }
            if (holidayWork) {
                holidayWorkDays++;
            }
            paidLeave += leave;
        }

        Totals totals() {
            return new Totals(total, overtime, workDays, holidayWorkDays, paidLeave);
        }
    }
}
