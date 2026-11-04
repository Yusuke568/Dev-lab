package com.example.kintai.application.port.in;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

/**
 * 勤怠のカレンダーで休みとして扱う日（国民の祝日と会社休日）を求めるユースケース。
 */
public interface GetHolidaysUseCase {

    /** 月の休み（日付 → 名前）。祝日と会社休日が重なる日は両方の名前を「・」でつなぐ。 */
    Map<LocalDate, String> getHolidays(YearMonth yearMonth);
}
