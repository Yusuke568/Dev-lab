package com.example.kintai.domain.model.attendance;

import java.time.YearMonth;
import java.util.List;

/**
 * 作業報告書Excelの1シート（1ヶ月分）から読み取った取り込みデータ。
 */
public class ImportedMonthData {

    private final YearMonth yearMonth;
    private final List<ImportedDayData> days;
    private final List<WeeklyWorkReport> weeklyWorkReports;

    public ImportedMonthData(YearMonth yearMonth, List<ImportedDayData> days, List<WeeklyWorkReport> weeklyWorkReports) {
        this.yearMonth = yearMonth;
        this.days = days;
        this.weeklyWorkReports = weeklyWorkReports;
    }

    public YearMonth getYearMonth() { return yearMonth; }
    public List<ImportedDayData> getDays() { return days; }
    public List<WeeklyWorkReport> getWeeklyWorkReports() { return weeklyWorkReports; }
}
