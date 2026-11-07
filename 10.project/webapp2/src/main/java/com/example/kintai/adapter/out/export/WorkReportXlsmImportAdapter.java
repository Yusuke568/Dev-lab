package com.example.kintai.adapter.out.export;

import com.example.kintai.domain.model.attendance.ImportedDayData;
import com.example.kintai.domain.model.attendance.ImportedMonthData;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.port.out.WorkReportImportPort;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 社内所定の「作業報告書」Excel（.xlsm）を読み取り、勤怠データ・週次作業報告へ変換するアダプタ。
 * WorkReportXlsmExportAdapterで書き込む内容の逆変換にあたる。
 */
public class WorkReportXlsmImportAdapter implements WorkReportImportPort {

    private static final String[] MONTH_SHEET_NAMES = {
            "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月", "前年12月"
    };

    private static final int FIRST_DAY_ROW = 7; // 0始まり（Excel8行目）
    private static final int[] WEEK_BLOCK_FIRST_ROWS = {5, 15, 25, 35, 45};

    // テンプレートのプレースホルダー文言（未入力のままの値は取り込み対象から除外する）
    private static final Set<String> PLACEHOLDER_VALUES = Set.of(
            "(客先名)", "・(PJ名)", "　(工程名・作業名等)",
            "作業進捗：(ｵﾝｽｹ，〇日遅れ等)", "問題点・対処：",
            "(問題とその対処を記載)", "(問題なければ'問題なし'と記載)"
    );

    @Override
    public List<ImportedMonthData> parse(InputStream xlsmInputStream) {
        List<ImportedMonthData> results = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(xlsmInputStream)) {
            for (String sheetName : MONTH_SHEET_NAMES) {
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    continue;
                }
                YearMonth yearMonth = resolveYearMonth(sheet);
                if (yearMonth == null) {
                    continue; // 年月が未設定のシートは対象外
                }

                List<ImportedDayData> days = parseDays(sheet, yearMonth);
                List<WeeklyWorkReport> weeklyWorkReports = parseWeeklyWorkReports(sheet, yearMonth);
                results.add(new ImportedMonthData(yearMonth, days, weeklyWorkReports));
            }
        } catch (Exception e) {
            throw new RuntimeException("作業報告書の読み込みに失敗しました。ファイル形式をご確認ください。", e);
        }
        return results;
    }

    private YearMonth resolveYearMonth(Sheet sheet) {
        Double year = getNumericValue(sheet, "A1");
        Double month = getNumericValue(sheet, "F1");
        if (year == null || month == null || year < 2000 || month < 1 || month > 12) {
            return null;
        }
        return YearMonth.of(year.intValue(), month.intValue());
    }

    private List<ImportedDayData> parseDays(Sheet sheet, YearMonth yearMonth) {
        List<ImportedDayData> days = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            int row = FIRST_DAY_ROW + day - 1;
            LocalDate date = yearMonth.atDay(day);

            String startTime = getTimeValue(sheet, row, "E");
            String endTime = getTimeValue(sheet, row, "F");
            Integer correctionUsTime = getMinutesValue(sheet, row, "H");
            Integer correctionMidTime = getMinutesValue(sheet, row, "J");
            Integer indirectTime = getMinutesValue(sheet, row, "K");
            String category = getStringValue(sheet, row, "AN");
            String memo = getStringValue(sheet, row, "AO");

            ImportedDayData dayData = new ImportedDayData(date, startTime, endTime,
                    correctionUsTime, correctionMidTime, indirectTime, category, memo);
            if (!dayData.isEmpty()) {
                days.add(dayData);
            }
        }
        return days;
    }

    private List<WeeklyWorkReport> parseWeeklyWorkReports(Sheet sheet, YearMonth yearMonth) {
        List<LocalDate[]> ranges = WeeklyWorkReport.resolveWeekRanges(yearMonth);
        List<WeeklyWorkReport> reports = new ArrayList<>();

        for (int i = 0; i < WEEK_BLOCK_FIRST_ROWS.length && i < ranges.size(); i++) {
            int baseRow = WEEK_BLOCK_FIRST_ROWS[i];
            LocalDate weekStart = ranges.get(i)[0];
            LocalDate weekEnd = ranges.get(i)[1];

            String clientName = cleanPlaceholder(getStringValue(sheet, "AR" + baseRow));
            String projectName = stripPrefix(cleanPlaceholder(getStringValue(sheet, "AR" + (baseRow + 1))), "・");
            // 工程名・作業名等は複数行（週ブロックの3〜7行目、各行の先頭は字下げの全角空白）
            String processName = joinLines(sheet, "AR", baseRow + 2, 5, "　");

            String progressStatus = stripPrefix(cleanPlaceholder(getStringValue(sheet, "AS" + baseRow)), "作業進捗：");
            String progressDetail = joinLines(sheet, "AS", baseRow + 1, 6);
            String personalNote = joinLines(sheet, "AT", baseRow, 10);
            String problemCountermeasure = readProblemCountermeasure(sheet, baseRow);
            // リーダー所見（AU列、本人所感と同じ10行）
            String leaderComment = joinLines(sheet, "AU", baseRow, 10);

            boolean hasAnyContent = hasText(clientName) || hasText(projectName) || hasText(processName)
                    || hasText(progressStatus) || hasText(progressDetail) || hasText(personalNote)
                    || hasText(problemCountermeasure) || hasText(leaderComment);
            if (hasAnyContent) {
                reports.add(new WeeklyWorkReport(null, weekStart, weekEnd,
                        clientName, projectName, processName, progressStatus, progressDetail, personalNote,
                        problemCountermeasure).withLeaderComment(leaderComment));
            }
        }
        return reports;
    }

    private String joinLines(Sheet sheet, String column, int startRow, int maxLines) {
        return joinLines(sheet, column, startRow, maxLines, "");
    }

    /**
     * 各行の先頭の prefix（様式の字下げなど）を外してつなげる。
     * 途中の空行も残す（隣の列の行と位置をそろえて書くため）。末尾の空行は除く。
     */
    private String joinLines(Sheet sheet, String column, int startRow, int maxLines, String prefix) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < maxLines; i++) {
            String value = stripPrefix(cleanPlaceholder(getStringValue(sheet, column + (startRow + i))), prefix);
            lines.add(hasText(value) ? value : "");
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

    /** 問題点・対処。見出しの行（「問題点・対処：問題なし」）に続けて書かれた分と、その下の2行を読む。 */
    private String readProblemCountermeasure(Sheet sheet, int baseRow) {
        String label = getStringValue(sheet, "AS" + (baseRow + 7));
        String inline = label != null && label.startsWith("問題点・対処：") ? label.substring("問題点・対処：".length()).trim() : "";
        String below = joinLines(sheet, "AS", baseRow + 8, 2);
        if (inline.isEmpty()) {
            return below;
        }
        return below == null ? inline : inline + "\n" + below;
    }

    private String cleanPlaceholder(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return PLACEHOLDER_VALUES.contains(trimmed) ? null : value;
    }

    private String stripPrefix(String value, String prefix) {
        if (value == null) return null;
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String getTimeValue(Sheet sheet, int row, String column) {
        Double fraction = getNumericValue(sheet, column + (row + 1));
        if (fraction == null) {
            return null;
        }
        int totalMinutes = (int) Math.round(fraction * 1440);
        // 24:00 以降（「25:00」など）は日をまたいだ退勤。翌日の時刻（1:00 など）にすると
        // 出勤時刻より前になるので、保存時に翌日の退勤として扱われる（24:00 は翌日の 0:00）。
        if (totalMinutes >= 1440) {
            totalMinutes -= 1440;
        }
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private Integer getMinutesValue(Sheet sheet, int row, String column) {
        Double fraction = getNumericValue(sheet, column + (row + 1));
        if (fraction == null) {
            return null;
        }
        return (int) Math.round(fraction * 1440);
    }

    private String getStringValue(Sheet sheet, int row, String column) {
        return getStringValue(sheet, column + (row + 1));
    }

    private String getStringValue(Sheet sheet, String cellRef) {
        Cell cell = getCell(sheet, cellRef);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            return (value == null || value.isBlank()) ? null : value;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return null;
    }

    private Double getNumericValue(Sheet sheet, String cellRef) {
        Cell cell = getCell(sheet, cellRef);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Cell getCell(Sheet sheet, String cellRef) {
        CellReference ref = new CellReference(cellRef);
        Row row = sheet.getRow(ref.getRow());
        if (row == null) {
            return null;
        }
        return row.getCell(ref.getCol());
    }
}
