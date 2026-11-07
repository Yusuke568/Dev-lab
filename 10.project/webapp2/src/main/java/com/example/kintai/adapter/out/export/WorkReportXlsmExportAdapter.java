package com.example.kintai.adapter.out.export;

import com.example.entity.WorkType;
import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.port.out.WorkReportExportPort;
import com.example.kintai.domain.service.WorkReportCategoryMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社内所定の「作業報告書」Excel（.xlsm）テンプレートに月次勤怠データを埋め込むアダプタ。
 *
 * テンプレートは src/main/resources/templates/work_report_template.xlsm にあり、
 * 元ファイルに組み込まれていたVBAマクロ「データ削除」機能（clearSheet_Timecard）と
 * 同じ範囲をあらかじめクリアした、個人データを含まない状態のものを使用する。
 * 出退勤・補正・摘要・備考以外のセル（総勤務・残業・深夜勤務・法定休出・労基法チェック等）は
 * すべて様式側の数式で自動計算されるため、ここでは一切書き換えない。
 */
public class WorkReportXlsmExportAdapter implements WorkReportExportPort {

    private static final String TEMPLATE_PATH = "/templates/work_report_template.xlsm";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 「摘要」欄（AN列）のプルダウンの選択肢が入っている範囲（様式の入力規則と同じ）
    private static final String CATEGORY_OPTIONS_COLUMN = "AX";
    private static final int CATEGORY_OPTIONS_FIRST_ROW = 14;
    private static final int CATEGORY_OPTIONS_LAST_ROW = 22;
    private static final String SIGN_PLUS = "＋";

    // 1日目のデータはExcel上の8行目から始まる（B8=1, B9=2, ...）。POIの行番号は0始まりのため7を基準にする。
    private static final int FIRST_DAY_ROW = 7;

    // 週次作業報告ブロックの先頭行（Excel行番号、1始まり）。様式は月あたり5週固定で10行おきに配置されている。
    private static final int[] WEEK_BLOCK_FIRST_ROWS = {5, 15, 25, 35, 45};

    // 各セルの列幅に収まるよう、週次作業報告の文字数上限を設ける（超過分は「…」で省略）。
    private static final int MAX_LEN_CLIENT_NAME = 20;
    private static final int MAX_LEN_PROJECT_NAME = 18;
    private static final int MAX_LEN_PROCESS_NAME = 18;
    // 工程名・作業名等は、PJ名の下から「累計/残業時間」の上までの5行（週ブロックの3〜7行目）に書ける
    private static final int MAX_LINES_PROCESS_NAME = 5;
    private static final int MAX_LEN_PROGRESS_STATUS = 10;
    private static final int MAX_LEN_PROGRESS_DETAIL_LINE = 20;
    private static final int MAX_LEN_PERSONAL_NOTE_LINE = 15; // 本人所感は1行15文字
    private static final int MAX_LEN_PROBLEM_LINE = 20;
    private static final String PROBLEM_LABEL = "問題点・対処：";

    @Override
    public byte[] generate(MonthlyAttendanceDto monthlyData, String employeeName, List<WorkType> workTypes,
                            List<WeeklyWorkReport> weeklyWorkReports) {
        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new IllegalStateException("作業報告書テンプレートが見つかりません: " + TEMPLATE_PATH);
            }
            try (XSSFWorkbook workbook = new XSSFWorkbook(templateStream)) {
                YearMonth yearMonth = monthlyData.getYearMonth();
                String sheetName = yearMonth.getMonthValue() + "月";
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IllegalStateException("テンプレートにシートが見つかりません: " + sheetName);
                }

                setNumericValue(sheet, "A1", yearMonth.getYear());
                setStringValue(sheet, "AP2", employeeName);

                // 勤務区分ID → 摘要欄の値（様式のプルダウンにある値だけ）
                List<String> categoryOptions = readCategoryOptions(sheet);
                Map<Integer, String> categoryByWorkTypeId = new HashMap<>();
                if (workTypes != null) {
                    for (WorkType workType : workTypes) {
                        WorkReportCategoryMapper.toCategory(workType, categoryOptions)
                                .ifPresent(category -> categoryByWorkTypeId.put(workType.getId(), category));
                    }
                }

                for (DailyAttendanceDto daily : monthlyData.getDailyRecords()) {
                    int row = FIRST_DAY_ROW + daily.getDate().getDayOfMonth() - 1;
                    writeDay(sheet, row, daily, categoryByWorkTypeId);
                }

                writeWeeklyWorkReports(sheet, weeklyWorkReports);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("作業報告書の生成に失敗しました。", e);
        }
    }

    private void writeDay(Sheet sheet, int row, DailyAttendanceDto daily, Map<Integer, String> categoryByWorkTypeId) {
        if (!"-".equals(daily.getStartTime())) {
            setTimeValue(sheet, row, "E", daily.getStartTime());
        }
        if (!"-".equals(daily.getEndTime())) {
            // 退勤が出勤より前の時刻なら日をまたいだ退勤なので、様式どおり「25:00」のように24時間を足す
            boolean endsNextDay = !"-".equals(daily.getStartTime())
                    && LocalTime.parse(daily.getEndTime(), TIME_FORMATTER).isBefore(LocalTime.parse(daily.getStartTime(), TIME_FORMATTER));
            setTimeValue(sheet, row, "F", daily.getEndTime(), endsNextDay);
        }

        Integer correctionUsTime = daily.getCorrectionUsTime();
        if (correctionUsTime != null && correctionUsTime > 0) {
            setStringValue(sheet, row, "G", SIGN_PLUS);
            setMinutesAsTimeValue(sheet, row, "H", correctionUsTime);
        }

        Integer correctionMidTime = daily.getCorrectionMidTime();
        if (correctionMidTime != null && correctionMidTime > 0) {
            setStringValue(sheet, row, "I", SIGN_PLUS);
            setMinutesAsTimeValue(sheet, row, "J", correctionMidTime);
        }

        Integer indirectTime = daily.getIndirectTime();
        if (indirectTime != null && indirectTime > 0) {
            setMinutesAsTimeValue(sheet, row, "K", indirectTime);
        }

        // 摘要（AN列）：勤務区分を様式のプルダウンの値で書く。選択肢にない区分（休日など）は空欄
        String category = daily.getAbstractId() != null ? categoryByWorkTypeId.get(daily.getAbstractId()) : null;
        if (category != null) {
            setStringValue(sheet, row, "AN", category);
        }

        String memo = daily.getWorkDescription();
        if (memo != null && !memo.isBlank()) {
            setStringValue(sheet, row, "AO", memo);
        }
    }

    /** 様式の「摘要」欄のプルダウンの選択肢（AX14:AX22）を読む。 */
    private List<String> readCategoryOptions(Sheet sheet) {
        List<String> options = new ArrayList<>();
        int col = CellReference.convertColStringToIndex(CATEGORY_OPTIONS_COLUMN);
        for (int r = CATEGORY_OPTIONS_FIRST_ROW; r <= CATEGORY_OPTIONS_LAST_ROW; r++) {
            Row row = sheet.getRow(r - 1);
            Cell cell = row != null ? row.getCell(col) : null;
            if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isBlank()) {
                options.add(cell.getStringCellValue().trim());
            }
        }
        return options;
    }

    /**
     * 週次作業報告（客先名・PJ名・工程名・作業進捗・本人所感）を、月の週ブロック構成に従って
     * AR/AS/AT列の該当ブロックへ書き込む。リーダー所見（AU列）は対象外（様式側で手入力）。
     */
    private void writeWeeklyWorkReports(Sheet sheet, List<WeeklyWorkReport> weeklyWorkReports) {
        if (weeklyWorkReports == null) {
            return;
        }
        for (int i = 0; i < weeklyWorkReports.size() && i < WEEK_BLOCK_FIRST_ROWS.length; i++) {
            WeeklyWorkReport report = weeklyWorkReports.get(i);
            int baseRow = WEEK_BLOCK_FIRST_ROWS[i]; // Excel行番号（1始まり）

            if (hasText(report.getClientName())) {
                setStringValue(sheet, "AR" + baseRow, truncate(report.getClientName(), MAX_LEN_CLIENT_NAME));
            }
            if (hasText(report.getProjectName())) {
                setStringValue(sheet, "AR" + (baseRow + 1), "・" + truncate(report.getProjectName(), MAX_LEN_PROJECT_NAME));
            }
            writeLines(sheet, "AR", baseRow + 2, MAX_LINES_PROCESS_NAME, report.getProcessName(), MAX_LEN_PROCESS_NAME, "　");

            if (hasText(report.getProgressStatus())) {
                setStringValue(sheet, "AS" + baseRow, "作業進捗：" + truncate(report.getProgressStatus(), MAX_LEN_PROGRESS_STATUS));
            }
            writeLines(sheet, "AS", baseRow + 1, 6, report.getProgressDetail(), MAX_LEN_PROGRESS_DETAIL_LINE);
            writeProblemCountermeasure(sheet, baseRow, report.getProblemCountermeasure());

            writeLines(sheet, "AT", baseRow, 10, report.getPersonalNote(), MAX_LEN_PERSONAL_NOTE_LINE);
        }
    }

    /**
     * 問題点・対処を書く。様式は AS列の8行目が見出し「問題点・対処：」、その下の2行が記入欄。
     * 1行で見出しと合わせて1行に収まるとき（「問題なし」など）は、見出しの行に続けて書く。
     * 書くときは記入欄の説明文（「(問題とその対処を記載)」など）を消す。
     */
    private void writeProblemCountermeasure(Sheet sheet, int baseRow, String text) {
        if (!hasText(text)) {
            return;
        }
        List<String> lines = new ArrayList<>(List.of(text.split("\\r?\\n")));
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        setStringValue(sheet, "AS" + (baseRow + 8), "");
        setStringValue(sheet, "AS" + (baseRow + 9), "");
        if (lines.size() == 1 && PROBLEM_LABEL.length() + lines.get(0).trim().length() <= MAX_LEN_PROBLEM_LINE) {
            setStringValue(sheet, "AS" + (baseRow + 7), PROBLEM_LABEL + lines.get(0).trim());
        } else {
            writeLines(sheet, "AS", baseRow + 8, 2, String.join("\n", lines), MAX_LEN_PROBLEM_LINE);
        }
    }

    /** 改行区切りのテキストを、指定列の連続する行に1行ずつ書き込む（最大maxLines行、各行maxLenChars文字まで）。 */
    private void writeLines(Sheet sheet, String column, int startRow, int maxLines, String text, int maxLenChars) {
        writeLines(sheet, column, startRow, maxLines, text, maxLenChars, "");
    }

    /** 各行の先頭に prefix（様式の字下げなど）を付けて書き込む。 */
    private void writeLines(Sheet sheet, String column, int startRow, int maxLines, String text, int maxLenChars,
                            String prefix) {
        if (!hasText(text)) {
            return;
        }
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length && i < maxLines; i++) {
            if (!lines[i].isBlank()) {
                setStringValue(sheet, column + (startRow + i), prefix + truncate(lines[i], maxLenChars));
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** セルの列幅に収まるよう、maxLen文字を超える分は末尾を省略記号（…）に置き換える。 */
    private String truncate(String value, int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLen - 1)) + "…";
    }

    private void setTimeValue(Sheet sheet, int row, String column, String hhmm) {
        setTimeValue(sheet, row, column, hhmm, false);
    }

    /** 時刻を Excel の時刻値（1日=1.0）で書く。翌日なら1日分を足す（25:00 = 1.0417）。 */
    private void setTimeValue(Sheet sheet, int row, String column, String hhmm, boolean nextDay) {
        LocalTime time = LocalTime.parse(hhmm, TIME_FORMATTER);
        double fraction = (time.getHour() * 60 + time.getMinute()) / 1440.0 + (nextDay ? 1 : 0);
        setNumericValue(sheet, column + (row + 1), fraction);
    }

    private void setMinutesAsTimeValue(Sheet sheet, int row, String column, int minutes) {
        setNumericValue(sheet, column + (row + 1), minutes / 1440.0);
    }

    private void setStringValue(Sheet sheet, int row, String column, String value) {
        setStringValue(sheet, column + (row + 1), value);
    }

    private void setStringValue(Sheet sheet, String cellRef, String value) {
        Cell cell = resolveCell(sheet, cellRef);
        cell.setCellValue(value);
    }

    private void setNumericValue(Sheet sheet, String cellRef, double value) {
        Cell cell = resolveCell(sheet, cellRef);
        cell.setCellValue(value);
    }

    /**
     * 既存セルを一旦削除してから作り直す。
     * テンプレート側で文字列として値が入っていたセル（t="inlineStr"）に対して
     * {@code Cell#setCellValue(String)} を直接呼ぶと、POIが古い値をinline stringとして
     * 残したまま新しい値をvタグ側にだけ書き込んでしまい、Excel上は古い値が優先表示される
     * （セルが壊れた状態になる）事象を確認したため、作り直す方式にしている。
     * 書式（時刻表示等）はセルスタイルを退避・再適用することで保持する。
     */
    private Cell resolveCell(Sheet sheet, String cellRef) {
        CellReference ref = new CellReference(cellRef);
        Row row = sheet.getRow(ref.getRow());
        if (row == null) {
            row = sheet.createRow(ref.getRow());
        }
        Cell existing = row.getCell(ref.getCol());
        CellStyle style = existing != null ? existing.getCellStyle() : null;
        if (existing != null) {
            row.removeCell(existing);
        }
        Cell cell = row.createCell(ref.getCol());
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }
}
