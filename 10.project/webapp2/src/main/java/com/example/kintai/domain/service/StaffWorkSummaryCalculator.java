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
 *   日曜（法定休日）の勤務は時間外ではなく「法定休出」として別に数える。
 *   36協定の「時間外＋法定休日労働」の上限（月100時間未満・2〜6か月の平均80時間以内）は、時間外と法定休出を足して見る。
 */
public final class StaffWorkSummaryCalculator {

    public static final int MONTHLY_LIMIT_MINUTES = 45 * 60;
    public static final int YEARLY_LIMIT_MINUTES = 540 * 60;
    /** 時間外＋法定休出の月の上限（100時間未満） */
    public static final int COMBINED_MONTHLY_LIMIT_MINUTES = 100 * 60;
    /** 時間外＋法定休出の2〜6か月の平均の上限（80時間以内） */
    public static final int COMBINED_AVERAGE_LIMIT_MINUTES = 80 * 60;

    private StaffWorkSummaryCalculator() {
    }

    /** 勤怠1日分（worked は出勤・退勤の両方が入っている日） */
    public record DayRow(LocalDate date, boolean worked, int totalMinutes, int overtimeMinutes, Integer abstractId) {
    }

    /** 残業事前申請1件（status は PENDING / APPROVED / REJECTED / WITHDRAWN、plannedMinutes は申請した時間外） */
    public record RequestRow(LocalDate date, String status, int plannedMinutes) {
        public RequestRow(LocalDate date, String status) {
            this(date, status, 0);
        }
    }

    /** 期間の集計 */
    public record Totals(int totalMinutes, int overtimeMinutes, int workDays, int holidayWorkDays, double paidLeaveDays,
                         int legalHolidayMinutes) {
    }

    /** 2〜6か月の平均のうち一番高いもの（months か月の平均が minutes） */
    public record Average(int months, int minutes) {
    }

    /**
     * 1人分のサマリー。
     * @param overtimeByMonth 1月〜表示する月までの、月ごとの時間外（添字0が1月）
     * @param pendingRequests 当月の申請中の残業事前申請の件数
     * @param approvedRequests 当月の承認済みの残業事前申請の件数
     * @param unappliedDays 当月の、時間外があるのに申請中・承認済みの事前申請がない日数
     * @param plannedApprovedMinutes 当月の、承認済みの事前申請の予定の時間外（まだ時間外の実績がない日の分）
     * @param plannedPendingMinutes  当月の、申請中の事前申請の予定の時間外（まだ時間外の実績がない日の分）
     * @param legalHolidayByMonth 1月〜表示する月までの、月ごとの法定休出（添字0が1月）
     * @param combinedLast6 表示する月までの6か月の、月ごとの時間外＋法定休出（添字5が表示する月。前年の月も含む）
     */
    public record Summary(Totals month, Totals year, int[] overtimeByMonth, Double paidLeaveRemaining,
                          int pendingRequests, int approvedRequests, int unappliedDays,
                          int plannedApprovedMinutes, int plannedPendingMinutes,
                          int[] legalHolidayByMonth, int[] combinedLast6) {

        /** 当月の時間外＋法定休出（時間外は実績＋予定の見込み） */
        public int monthCombinedMinutes() {
            return monthOutlookMinutes() + month.legalHolidayMinutes();
        }

        /** 当月の時間外＋法定休出の、月100時間に対する割合（%） */
        public int monthCombinedPercent() {
            return percent(monthCombinedMinutes(), COMBINED_MONTHLY_LIMIT_MINUTES);
        }

        /** 2〜6か月の平均（表示する月を最後の月とする）のうち一番高いもの。表示する月は見込み（予定を含む） */
        public Average maxAverage() {
            int[] values = combinedLast6.clone();
            values[values.length - 1] = monthCombinedMinutes();
            Average best = new Average(2, 0);
            for (int n = 2; n <= values.length; n++) {
                int sum = 0;
                for (int i = values.length - n; i < values.length; i++) sum += values[i];
                int avg = (int) Math.round(sum / (double) n);
                if (avg > best.minutes()) best = new Average(n, avg);
            }
            return best;
        }

        /** 2〜6か月の平均の一番高いものの、80時間に対する割合（%） */
        public int averagePercent() {
            return percent(maxAverage().minutes(), COMBINED_AVERAGE_LIMIT_MINUTES);
        }

        /** 当月の時間外の見込み（実績＋承認済み・申請中の予定） */
        public int monthOutlookMinutes() {
            return month.overtimeMinutes() + plannedApprovedMinutes + plannedPendingMinutes;
        }

        /** 当月の時間外の見込み（実績＋予定）の、上限（45時間）に対する割合（%） */
        public int monthOvertimePercent() {
            return percent(monthOutlookMinutes(), MONTHLY_LIMIT_MINUTES);
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
        int[] legalByMonth = new int[month.getMonthValue()];
        int[] last6 = new int[6];
        YearMonth first6 = month.minusMonths(5);
        Accumulator m = new Accumulator();
        Accumulator y = new Accumulator();
        Set<LocalDate> overtimeDays = new HashSet<>();
        for (DayRow d : days) {
            if (d.date().isAfter(end)) {
                continue;
            }
            int legal = legalHolidayMinutes(d);
            // 2〜6か月の平均用（前年の月も入る）
            YearMonth ym = YearMonth.from(d.date());
            if (!ym.isBefore(first6)) {
                last6[(int) java.time.temporal.ChronoUnit.MONTHS.between(first6, ym)] += Math.max(0, d.overtimeMinutes()) + legal;
            }
            if (d.date().isBefore(yearStart)) {
                continue;
            }
            legalByMonth[d.date().getMonthValue() - 1] += legal;
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
        int plannedApproved = 0;
        int plannedPending = 0;
        Set<LocalDate> covered = new HashSet<>();
        for (RequestRow r : requests) {
            if (!YearMonth.from(r.date()).equals(month)) {
                continue;
            }
            // 時間外の実績がある日は実績で数える（予定は足さない）
            int plan = overtimeDays.contains(r.date()) ? 0 : Math.max(0, r.plannedMinutes());
            if ("PENDING".equals(r.status())) {
                pending++;
                plannedPending += plan;
                covered.add(r.date());
            } else if ("APPROVED".equals(r.status())) {
                approved++;
                plannedApproved += plan;
                covered.add(r.date());
            }
        }
        overtimeDays.removeAll(covered);
        return new Summary(m.totals(), y.totals(), byMonth, paidLeaveRemaining, pending, approved, overtimeDays.size(),
                plannedApproved, plannedPending, legalByMonth, last6);
    }

    /** 法定休出：日曜（法定休日）の総勤務。日曜でなければ 0 */
    static int legalHolidayMinutes(DayRow d) {
        return WeeklyOvertimeCalculator.isLegalHoliday(d.date()) ? Math.max(0, d.totalMinutes()) : 0;
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
        int legal;

        void add(DayRow d, boolean holidayWork, double leave) {
            total += Math.max(0, d.totalMinutes());
            legal += legalHolidayMinutes(d);
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
            return new Totals(total, overtime, workDays, holidayWorkDays, paidLeave, legal);
        }
    }
}
