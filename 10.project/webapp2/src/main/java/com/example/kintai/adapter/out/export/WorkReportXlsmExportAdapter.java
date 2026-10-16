package com.example.kintai.adapter.out.export;

import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.port.out.WorkReportExportPort;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
import java.util.List;

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

    // 様式側のプルダウン選択肢（AX14:AX22）に定義されている正式な値をそのまま使う
    private static final String AN_PAID_LEAVE = "有休";
    private static final String SIGN_PLUS = "＋";

    // 1日目のデータはExcel上の8行目から始まる（B8=1, B9=2, ...）。POIの行番号は0始まりのため7を基準にする。
    private static final int FIRST_DAY_ROW = 7;

    // 週次作業報告ブロックの先頭行（Excel行番号、1始まり）。様式は月あたり5週固定で10行おきに配置されている。
    private static final int[] WEEK_BLOCK_FIRST_ROWS = {5, 15, 25, 35, 45};

    // 各セルの列幅に収まるよう、週次作業報告の文字数上限を設ける（超過分は「…」で省略）。
    private static final int MAX_LEN_CLIENT_NAME = 20;
    private static final int MAX_LEN_PROJECT_NAME = 18;
    private static final int MAX_LEN_PROCESS_NAME = 18;
    private static final int MAX_LEN_PROGRESS_STATUS = 10;
    private static final int MAX_LEN_PROGRESS_DETAIL_LINE = 20;
    private static final int MAX_LEN_PERSONAL_NOTE_LINE = 20;

    @Override
    public byte[] generate(MonthlyAttendanceDto monthlyData, String employeeName, Integer paidLeaveWorkTypeId,
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

                for (DailyAttendanceDto daily : monthlyData.getDailyRecords()) {
                    int row = FIRST_DAY_ROW + daily.getDate().getDayOfMonth() - 1;
                    writeDay(sheet, row, daily, paidLeaveWorkTypeId);
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

    private void writeDay(Sheet sheet, int row, DailyAttendanceDto daily, Integer paidLeaveWorkTypeId) {
        if (!"-".equals(daily.getStartTime())) {
            setTimeValue(sheet, row, "E", daily.getStartTime());
        }
        if (!"-".equals(daily.getEndTime())) {
            setTimeValue(sheet, row, "F", daily.getEndTime());
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

        if (paidLeaveWorkTypeId != null && paidLeaveWorkTypeId.equals(daily.getAbstractId())) {
            setStringValue(sheet, row, "AN", AN_PAID_LEAVE);
        }

        String memo = daily.getWorkDescription();
        if (memo != null && !memo.isBlank()) {
            setStringValue(sheet, row, "AO", memo);
        }
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
            if (hasText(report.getProcessName())) {
                setStringValue(sheet, "AR" + (baseRow + 2), "　" + truncate(report.getProcessName(), MAX_LEN_PROCESS_NAME));
            }

            if (hasText(report.getProgressStatus())) {
                setStringValue(sheet, "AS" + baseRow, "作業進捗：" + truncate(report.getProgressStatus(), MAX_LEN_PROGRESS_STATUS));
            }
            writeLines(sheet, "AS", baseRow + 1, 6, report.getProgressDetail(), MAX_LEN_PROGRESS_DETAIL_LINE);

            writeLines(sheet, "AT", baseRow, 10, report.getPersonalNote(), MAX_LEN_PERSONAL_NOTE_LINE);
        }
    }

    /** 改行区切りのテキストを、指定列の連続する行に1行ずつ書き込む（最大maxLines行、各行maxLenChars文字まで）。 */
    private void writeLines(Sheet sheet, String column, int startRow, int maxLines, String text, int maxLenChars) {
        if (!hasText(text)) {
            return;
        }
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length && i < maxLines; i++) {
            if (!lines[i].isBlank()) {
                setStringValue(sheet, column + (startRow + i), truncate(lines[i], maxLenChars));
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
        LocalTime time = LocalTime.parse(hhmm, TIME_FORMATTER);
        double fraction = (time.getHour() * 60 + time.getMinute()) / 1440.0;
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
