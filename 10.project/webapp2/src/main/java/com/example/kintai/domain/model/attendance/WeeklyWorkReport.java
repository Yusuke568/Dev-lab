package com.example.kintai.domain.model.attendance;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 週次作業報告（木曜始まり・翌水曜終わりの週単位）。
 * 作業報告書Excel様式の「作業件名、工程名・作業名」「作業進捗、作業内容と状況」「本人所感」欄に対応する。
 *
 * 1つの月は必ず5週分のブロックに分解される（作業報告書Excel様式が月あたり5週固定のため）。
 * 各週は水曜日（weekEndDate）を一意キーとして管理し、月をまたぐ週もそのまま許容する。
 */
public class WeeklyWorkReport {

    private final String employeeId;
    private final LocalDate weekStartDate; // 木曜日（月初に重なる場合は月初日）
    private final LocalDate weekEndDate;   // 水曜日
    private final String clientName;
    private final String projectName;
    private final String processName;
    private final String progressStatus;
    private final String progressDetail;
    private final String personalNote;

    public WeeklyWorkReport(String employeeId, LocalDate weekStartDate, LocalDate weekEndDate,
                             String clientName, String projectName, String processName,
                             String progressStatus, String progressDetail, String personalNote) {
        this.employeeId = employeeId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.clientName = clientName;
        this.projectName = projectName;
        this.processName = processName;
        this.progressStatus = progressStatus;
        this.progressDetail = progressDetail;
        this.personalNote = personalNote;
    }

    public String getEmployeeId() { return employeeId; }
    public LocalDate getWeekStartDate() { return weekStartDate; }
    public LocalDate getWeekEndDate() { return weekEndDate; }
    public String getClientName() { return clientName; }
    public String getProjectName() { return projectName; }
    public String getProcessName() { return processName; }
    public String getProgressStatus() { return progressStatus; }
    public String getProgressDetail() { return progressDetail; }
    public String getPersonalNote() { return personalNote; }

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("M/d");

    /** 画面表示用のラベル（例: "9/3(木) ～ 9/9(水)"）。 */
    public String getWeekLabel() {
        return weekStartDate.format(LABEL_FORMATTER) + "(" + dayOfWeekJa(weekStartDate) + ") ～ "
                + weekEndDate.format(LABEL_FORMATTER) + "(" + dayOfWeekJa(weekEndDate) + ")";
    }

    private static String dayOfWeekJa(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "月";
            case TUESDAY: return "火";
            case WEDNESDAY: return "水";
            case THURSDAY: return "木";
            case FRIDAY: return "金";
            case SATURDAY: return "土";
            default: return "日";
        }
    }

    /**
     * 指定された年月を、作業報告書Excel様式の週ブロック定義（木～翌水、月あたり5週固定）に
     * 従って5つの週区間に分解し、各週の開始日（木曜 or 月初）・終了日（水曜）を返す。
     *
     * 様式側の週末尾算出ロジック（AW5セル）を踏襲: 第1週の終了日は月初以降で最初に訪れる水曜日。
     * 以降、7日おきに次の週の終了日となる。月をまたいでも週としてはそのまま扱う。
     */
    public static List<LocalDate[]> resolveWeekRanges(YearMonth yearMonth) {
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate firstWeekEnd = firstOfMonth;
        while (firstWeekEnd.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            firstWeekEnd = firstWeekEnd.plusDays(1);
        }

        List<LocalDate[]> ranges = new ArrayList<>();
        LocalDate weekEnd = firstWeekEnd;
        LocalDate weekStart = firstOfMonth; // 第1週は月初始まり（前月分の木～水の残りは対象外）
        for (int i = 0; i < 5; i++) {
            ranges.add(new LocalDate[]{weekStart, weekEnd});
            weekStart = weekEnd.plusDays(1); // 次の木曜日
            weekEnd = weekEnd.plusDays(7);
        }
        return ranges;
    }
}
