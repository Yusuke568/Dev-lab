package com.example.kintai.domain.model.attendance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 週次作業報告の「累計/残業時間」欄（作業報告書Excelの週ブロック、AR列の8〜10行目）に出る値。
 * Excel 側は数式で計算するので、画面で同じ内容を確認できるように勤怠から計算した値を持つ。
 *
 * - 週：その週（木〜水）の稼働時間／残業時間の合計
 * - 月：月初からその週の水曜日まで（月末を超えない）の稼働時間／残業時間の合計
 * - 前月：前月全体の稼働時間／残業時間の合計
 */
public class WeeklyWorkSummary {

    private static final DateTimeFormatter RANGE_FORMATTER = DateTimeFormatter.ofPattern("M/d");

    private final LocalDate rangeStart;
    private final LocalDate weekEnd;
    private final long weekWorkMinutes;
    private final long weekOvertimeMinutes;
    private final long monthWorkMinutes;
    private final long monthOvertimeMinutes;
    private final long previousMonthWorkMinutes;
    private final long previousMonthOvertimeMinutes;

    public WeeklyWorkSummary(LocalDate rangeStart, LocalDate weekEnd, long weekWorkMinutes, long weekOvertimeMinutes,
                             long monthWorkMinutes, long monthOvertimeMinutes,
                             long previousMonthWorkMinutes, long previousMonthOvertimeMinutes) {
        this.rangeStart = rangeStart;
        this.weekEnd = weekEnd;
        this.weekWorkMinutes = weekWorkMinutes;
        this.weekOvertimeMinutes = weekOvertimeMinutes;
        this.monthWorkMinutes = monthWorkMinutes;
        this.monthOvertimeMinutes = monthOvertimeMinutes;
        this.previousMonthWorkMinutes = previousMonthWorkMinutes;
        this.previousMonthOvertimeMinutes = previousMonthOvertimeMinutes;
    }

    public LocalDate getRangeStart() { return rangeStart; }
    public LocalDate getWeekEnd() { return weekEnd; }
    public long getWeekWorkMinutes() { return weekWorkMinutes; }
    public long getWeekOvertimeMinutes() { return weekOvertimeMinutes; }
    public long getMonthWorkMinutes() { return monthWorkMinutes; }
    public long getMonthOvertimeMinutes() { return monthOvertimeMinutes; }
    public long getPreviousMonthWorkMinutes() { return previousMonthWorkMinutes; }
    public long getPreviousMonthOvertimeMinutes() { return previousMonthOvertimeMinutes; }

    /** 1行目：「・10/29-11/4の累計/残業時間」 */
    public String getHeadingLine() {
        return "・" + rangeStart.format(RANGE_FORMATTER) + "-" + weekEnd.format(RANGE_FORMATTER) + "の累計/残業時間";
    }

    /** 2行目：「週8:00/0:00,月8:00/0:00」 */
    public String getTotalsLine() {
        return "週" + hours(weekWorkMinutes) + "/" + hours(weekOvertimeMinutes)
                + ",月" + hours(monthWorkMinutes) + "/" + hours(monthOvertimeMinutes);
    }

    /** 3行目：「(前月0:00/0:00)」 */
    public String getPreviousMonthLine() {
        return "(前月" + hours(previousMonthWorkMinutes) + "/" + hours(previousMonthOvertimeMinutes) + ")";
    }

    /** Excel の [h]:mm と同じ表記（時間は24を超えても繰り上げない）。 */
    static String hours(long minutes) {
        long abs = Math.abs(minutes);
        return (minutes < 0 ? "-" : "") + (abs / 60) + ":" + String.format("%02d", abs % 60);
    }
}
