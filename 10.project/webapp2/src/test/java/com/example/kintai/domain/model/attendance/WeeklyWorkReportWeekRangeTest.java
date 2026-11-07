package com.example.kintai.domain.model.attendance;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 週次作業報告の週（木〜水）。月をまたぐ週も木曜日から数える */
class WeeklyWorkReportWeekRangeTest {

    private static String label(LocalDate[] range) {
        return new WeeklyWorkReport("126", range[0], range[1], null, null, null, null, null, null).getWeekLabel();
    }

    @Test
    void 第1週は前月の木曜日から始まる() {
        // 2026年12月1日は火曜日 → 第1週は 11/26(木)〜12/2(水)
        List<LocalDate[]> weeks = WeeklyWorkReport.resolveWeekRanges(YearMonth.of(2026, 12));

        assertEquals("11/26(木) ～ 12/2(水)", label(weeks.get(0)));
        assertEquals("12/3(木) ～ 12/9(水)", label(weeks.get(1)));
        assertEquals("12/24(木) ～ 12/30(水)", label(weeks.get(4)));
    }

    @Test
    void 月初が木曜日ならその日から() {
        // 2026年10月1日は木曜日
        assertEquals("10/1(木) ～ 10/7(水)", label(WeeklyWorkReport.resolveWeekRanges(YearMonth.of(2026, 10)).get(0)));
    }

    @Test
    void どの週も7日間() {
        for (LocalDate[] range : WeeklyWorkReport.resolveWeekRanges(YearMonth.of(2027, 1))) {
            assertEquals(range[0].plusDays(6), range[1]);
        }
    }
}
