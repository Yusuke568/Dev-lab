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
    private final LocalDate weekStartDate; // 木曜日（第1週は前月の木曜日になることがある）
    private final LocalDate weekEndDate;   // 水曜日
    private final String clientName;
    private final String projectName;
    private final String processName;
    private final String progressStatus;
    private final String progressDetail;
    private final String personalNote;
    private final String problemCountermeasure; // 問題点・対処（2行）
    /** リーダー所見（作業報告書の AU列）。画面では編集できず、作業報告書の取り込みでだけ入る */
    private String leaderComment;
    /** 保存するときにリーダー所見も書き換えるか（取り込みのときだけ true。画面からの保存では今の所見を残す） */
    private boolean leaderCommentIncluded;

    public WeeklyWorkReport(String employeeId, LocalDate weekStartDate, LocalDate weekEndDate,
                             String clientName, String projectName, String processName,
                             String progressStatus, String progressDetail, String personalNote) {
        this(employeeId, weekStartDate, weekEndDate, clientName, projectName, processName,
                progressStatus, progressDetail, personalNote, null);
    }

    public WeeklyWorkReport(String employeeId, LocalDate weekStartDate, LocalDate weekEndDate,
                             String clientName, String projectName, String processName,
                             String progressStatus, String progressDetail, String personalNote,
                             String problemCountermeasure) {
        this.problemCountermeasure = problemCountermeasure;
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
    public String getProblemCountermeasure() { return problemCountermeasure; }
    public String getLeaderComment() { return leaderComment; }
    public boolean isLeaderCommentIncluded() { return leaderCommentIncluded; }

    /** 読み込んだリーダー所見を持たせる（DB・作業報告書から読んだとき） */
    public WeeklyWorkReport withLeaderComment(String leaderComment) {
        this.leaderComment = leaderComment;
        return this;
    }

    /** 保存のときにリーダー所見も書き換えるようにする（作業報告書の取り込み用） */
    public WeeklyWorkReport replacingLeaderComment(String leaderComment) {
        this.leaderComment = leaderComment;
        this.leaderCommentIncluded = true;
        return this;
    }

    /** 工程名・作業名等の各行（作業報告書の週ブロック3〜7行目。空行も位置を保つ）。 */
    public java.util.List<String> getProcessNameLines() { return toLines(processName, 5); }

    /** 作業内容と状況の各行（週ブロック2〜7行目。空行も位置を保つ）。 */
    public java.util.List<String> getProgressDetailLines() { return toLines(progressDetail, 6); }

    private static java.util.List<String> toLines(String text, int size) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text != null && !text.isEmpty()) {
            for (String line : text.split("\\r?\\n", -1)) {
                lines.add(line);
            }
        }
        while (lines.size() < size) {
            lines.add("");
        }
        return lines;
    }

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
     * 従って5つの週区間に分解し、各週の開始日（木曜）・終了日（水曜）を返す。
     * 月をまたぐ週も木曜から数える（例：2026年12月の第1週は 11/26(木)〜12/2(水)）。
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
        // 第1週も木曜日から（月初が木曜日でなければ前月の木曜日。週の稼働・残業の集計と同じ範囲）
        LocalDate weekStart = firstWeekEnd.minusDays(6);
        for (int i = 0; i < 5; i++) {
            ranges.add(new LocalDate[]{weekStart, weekEnd});
            weekStart = weekEnd.plusDays(1); // 次の木曜日
            weekEnd = weekEnd.plusDays(7);
        }
        return ranges;
    }
}
