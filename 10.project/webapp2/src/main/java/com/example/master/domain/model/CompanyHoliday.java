package com.example.master.domain.model;

import java.time.LocalDate;

/**
 * 会社休日（年末年始・創立記念日など、国民の祝日以外の会社の休日）。
 */
public class CompanyHoliday {

    private static final String[] DAY_OF_WEEK = {"月", "火", "水", "木", "金", "土", "日"};

    private final LocalDate date;
    private final String name;

    public CompanyHoliday(LocalDate date, String name) {
        this.date = date;
        this.name = name;
    }

    public LocalDate getDate() { return date; }
    public String getName() { return name; }

    /** 一覧に出す曜日（月〜日）。 */
    public String getDayOfWeekLabel() {
        return DAY_OF_WEEK[date.getDayOfWeek().getValue() - 1];
    }
}
