package com.example.kintai.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 週40時間の時間外（休日出勤の日の分）を計算する。
 *
 * 週は日曜〜土曜。月〜金に毎日8時間（週40時間）働く前提で、
 *   1. 月〜金の「所定に数える時間」を足す：その日の総勤務のうち8時間まで。祝日・会社休日は0時間
 *      （その日に働いた分は休日出勤として 2. で数える）、有給は8時間、半休は4時間（＋働いた分、合わせて8時間まで）として数える
 *   2. 休日出勤の日（土日・平日の祝日・会社休日）の勤務は、日付順に「40時間に足りない分」を埋め、
 *      埋めきった残りを時間外にする（1日に埋められるのは8時間まで）
 * 例：月〜金に8時間ずつ（40時間）働いた週の土曜の2時間 → 時間外2時間
 *     月〜金のうち1日が6時間（38時間）の週の土曜の4時間 → 2時間は40時間に足し、時間外2時間
 *     祝日のある週（月〜金のうち4日を8時間ずつ＝32時間）の土曜の8時間 → 全部40時間に足し、時間外なし
 * 平日（休日でない月〜金）の時間外は今までどおり、その日の総勤務のうち8時間を超えた分（ここでは扱わない）。
 */
public final class WeeklyOvertimeCalculator {

    public static final int DAILY_MINUTES = 8 * 60;
    public static final int WEEKLY_MINUTES = 40 * 60;

    private WeeklyOvertimeCalculator() {
    }

    /**
     * 1日分。
     * @param totalMinutes        その日の総勤務（補正・間接を含む）
     * @param holiday             休日（土日・祝日・会社休日）なら true。その日の勤務は休日出勤
     * @param leaveCreditMinutes  有給で所定に数える時間（有給 480、半休 240、それ以外 0）
     */
    public record Day(LocalDate date, int totalMinutes, boolean holiday, int leaveCreditMinutes) {
    }

    /** その日を含む週の日曜日 */
    public static LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    static boolean isWeekday(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }

    /** 月〜金の所定に数える時間（休日の平日は0時間。働いた分は休日出勤として数える） */
    static int weekdayCredit(Day day) {
        if (!isWeekday(day.date()) || day.holiday()) {
            return 0;
        }
        return Math.min(DAILY_MINUTES, day.totalMinutes() + day.leaveCreditMinutes());
    }

    /**
     * 休日出勤の日の時間外を返す（休日の日だけ。日付 → 時間外の分）。
     * @param week 同じ週（日曜〜土曜）の日。ない日は勤務なしとして扱う
     */
    public static Map<LocalDate, Integer> holidayOvertime(List<Day> week) {
        int baseline = week.stream().mapToInt(WeeklyOvertimeCalculator::weekdayCredit).sum();
        int remaining = Math.max(0, WEEKLY_MINUTES - baseline);
        Map<LocalDate, Integer> result = new LinkedHashMap<>();
        for (Day day : week.stream().filter(Day::holiday).sorted(Comparator.comparing(Day::date)).toList()) {
            int total = Math.max(0, day.totalMinutes());
            int regular = Math.min(total, Math.min(remaining, DAILY_MINUTES));
            remaining -= regular;
            result.put(day.date(), total - regular);
        }
        return result;
    }
}
