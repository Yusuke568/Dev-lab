package com.example.kintai.application.service;

import com.example.kintai.application.port.in.GetHolidaysUseCase;
import com.example.kintai.domain.port.out.CompanyHolidayPort;
import com.example.kintai.domain.service.JapaneseHolidayCalendar;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

/**
 * 国民の祝日（自動計算）と会社休日（マスタ）をまとめる。
 */
public class HolidayService implements GetHolidaysUseCase {

    private final CompanyHolidayPort companyHolidayPort;

    public HolidayService(CompanyHolidayPort companyHolidayPort) {
        this.companyHolidayPort = companyHolidayPort;
    }

    @Override
    public Map<LocalDate, String> getHolidays(YearMonth yearMonth) {
        Map<LocalDate, String> holidays = new TreeMap<>(JapaneseHolidayCalendar.holidaysOf(yearMonth));
        companyHolidayPort.findCompanyHolidays(yearMonth.atDay(1), yearMonth.atEndOfMonth())
                .forEach((date, name) -> holidays.merge(date, name, (national, company) -> national + "・" + company));
        return holidays;
    }
}
