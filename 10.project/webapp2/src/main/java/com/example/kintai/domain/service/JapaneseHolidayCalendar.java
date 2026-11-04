package com.example.kintai.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日本の祝日（「国民の祝日に関する法律」による休日）を計算する。
 *
 * - 2007年以降の法律に基づく（昭和の日・振替休日の規定）。2000〜2099年を対象にする
 * - 春分の日・秋分の日は、国立天文台の公表値と一致する近似式で求める
 * - 振替休日（祝日が日曜なら、その後の最初の平日）と国民の休日（祝日にはさまれた平日）も含める
 * - 2019年（即位関連）・2020〜2021年（東京五輪による移動）の特例を反映する
 */
public final class JapaneseHolidayCalendar {

    private static final Map<Integer, Map<LocalDate, String>> CACHE = new ConcurrentHashMap<>();

    private JapaneseHolidayCalendar() {
    }

    /** 日付が祝日なら、その名前。 */
    public static Optional<String> nameOf(LocalDate date) {
        return Optional.ofNullable(holidaysOf(date.getYear()).get(date));
    }

    public static boolean isHoliday(LocalDate date) {
        return holidaysOf(date.getYear()).containsKey(date);
    }

    /** 月の祝日（日付 → 名前）。 */
    public static Map<LocalDate, String> holidaysOf(YearMonth yearMonth) {
        Map<LocalDate, String> result = new TreeMap<>();
        holidaysOf(yearMonth.getYear()).forEach((date, name) -> {
            if (YearMonth.from(date).equals(yearMonth)) {
                result.put(date, name);
            }
        });
        return result;
    }

    /** 年の祝日（日付 → 名前）。 */
    public static Map<LocalDate, String> holidaysOf(int year) {
        if (year < 2000 || year > 2099) {
            return Collections.emptyMap();
        }
        return CACHE.computeIfAbsent(year, y -> Collections.unmodifiableMap(calculate(y)));
    }

    private static Map<LocalDate, String> calculate(int year) {
        TreeMap<LocalDate, String> days = new TreeMap<>();

        days.put(LocalDate.of(year, 1, 1), "元日");
        days.put(nthMonday(year, Month.JANUARY, 2), "成人の日");
        days.put(LocalDate.of(year, 2, 11), "建国記念の日");
        if (year >= 2020) {
            days.put(LocalDate.of(year, 2, 23), "天皇誕生日");
        }
        days.put(LocalDate.of(year, 3, vernalEquinoxDay(year)), "春分の日");
        days.put(LocalDate.of(year, 4, 29), year >= 2007 ? "昭和の日" : "みどりの日");
        days.put(LocalDate.of(year, 5, 3), "憲法記念日");
        if (year >= 2007) {
            days.put(LocalDate.of(year, 5, 4), "みどりの日");
        }
        days.put(LocalDate.of(year, 5, 5), "こどもの日");

        // 海の日・スポーツの日（体育の日）・山の日（東京五輪の年は移動）
        if (year == 2020) {
            days.put(LocalDate.of(2020, 7, 23), "海の日");
            days.put(LocalDate.of(2020, 7, 24), "スポーツの日");
            days.put(LocalDate.of(2020, 8, 10), "山の日");
        } else if (year == 2021) {
            days.put(LocalDate.of(2021, 7, 22), "海の日");
            days.put(LocalDate.of(2021, 7, 23), "スポーツの日");
            days.put(LocalDate.of(2021, 8, 8), "山の日");
        } else {
            days.put(year >= 2003 ? nthMonday(year, Month.JULY, 3) : LocalDate.of(year, 7, 20), "海の日");
            if (year >= 2016) {
                days.put(LocalDate.of(year, 8, 11), "山の日");
            }
            days.put(nthMonday(year, Month.OCTOBER, 2), year >= 2020 ? "スポーツの日" : "体育の日");
        }

        days.put(year >= 2003 ? nthMonday(year, Month.SEPTEMBER, 3) : LocalDate.of(year, 9, 15), "敬老の日");
        days.put(LocalDate.of(year, 9, autumnalEquinoxDay(year)), "秋分の日");
        days.put(LocalDate.of(year, 11, 3), "文化の日");
        days.put(LocalDate.of(year, 11, 23), "勤労感謝の日");
        if (year >= 2000 && year <= 2018) {
            days.put(LocalDate.of(year, 12, 23), "天皇誕生日");
        }

        // 2019年の即位関連
        if (year == 2019) {
            days.put(LocalDate.of(2019, 5, 1), "休日（天皇の即位の日）");
            days.put(LocalDate.of(2019, 10, 22), "休日（即位礼正殿の儀の行われる日）");
        }

        // 国民の休日：前日と翌日が祝日で、その日自身は祝日でない平日（日曜以外）
        TreeMap<LocalDate, String> sandwiched = new TreeMap<>();
        for (LocalDate date : days.keySet()) {
            LocalDate between = date.plusDays(1);
            if (!days.containsKey(between) && days.containsKey(between.plusDays(1))
                    && between.getDayOfWeek() != DayOfWeek.SUNDAY) {
                sandwiched.put(between, "国民の休日");
            }
        }
        days.putAll(sandwiched);

        // 振替休日：祝日が日曜なら、その後の最初の祝日でない日
        TreeMap<LocalDate, String> substitutes = new TreeMap<>();
        for (LocalDate date : days.keySet()) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                LocalDate next = date.plusDays(1);
                while (days.containsKey(next) || substitutes.containsKey(next)) {
                    next = next.plusDays(1);
                }
                substitutes.put(next, "振替休日");
            }
        }
        days.putAll(substitutes);

        // 年をまたいだ振替休日などは入れない
        days.keySet().removeIf(d -> d.getYear() != year);
        return days;
    }

    private static LocalDate nthMonday(int year, Month month, int n) {
        return LocalDate.of(year, month, 1).with(TemporalAdjusters.dayOfWeekInMonth(n, DayOfWeek.MONDAY));
    }

    /** 春分日（1980〜2099年で国立天文台の値と一致する近似式）。 */
    static int vernalEquinoxDay(int year) {
        return (int) (20.8431 + 0.242194 * (year - 1980) - Math.floor((year - 1980) / 4.0));
    }

    /** 秋分日（同上）。 */
    static int autumnalEquinoxDay(int year) {
        return (int) (23.2488 + 0.242194 * (year - 1980) - Math.floor((year - 1980) / 4.0));
    }
}
