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
import java.time.LocalDate;
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

    // 週次作業報告の1行に入る幅（半角1・全角2で数える）。AR〜AU列はどれも全角15文字（半角30文字）分。
    // 手作業で書いた作業報告書の行もこの幅に収まっている。長い行は文字で切らずに次の行へ送る
    static final int LINE_WIDTH = 30;
    // 工程名・作業名等は、PJ名の下から「累計/残業時間」の上までの5行（週ブロックの3〜7行目）に書ける
    private static final int MAX_LINES_PROCESS_NAME = 5;
    private static final String PROBLEM_LABEL = "問題点・対処：";

    @Override
    public byte[] generate(MonthlyAttendanceDto monthlyData, String employeeName, List<WorkType> workTypes,
                            List<WeeklyWorkReport> weeklyWorkReports) {
        return generate(monthlyData, employeeName, null, workTypes, weeklyWorkReports);
    }

    @Override
    public byte[] generate(MonthlyAttendanceDto monthlyData, String employeeName, String departmentName,
                           List<WorkType> workTypes, List<WeeklyWorkReport> weeklyWorkReports) {
        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new IllegalStateException("作業報告書テンプレートが見つかりません: " + TEMPLATE_PATH);
            }
            try (XSSFWorkbook workbook = new XSSFWorkbook(templateStream)) {
                YearMonth yearMonth = monthlyData.getYearMonth();
                Sheet sheet = sheetOf(workbook, yearMonth, yearMonth.getYear());
                setNumericValue(sheet, "A1", yearMonth.getYear());
                setStringValue(sheet, "AP2", employeeName);
                setDepartment(sheet, departmentName);
                writeMonth(sheet, monthlyData, weeklyWorkReports, workTypes);
                focus(workbook, yearMonth, yearMonth.getYear(), LocalDate.now());

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // 様式の数式（総勤務・残業・深夜など）の計算結果を、出力のときに入れておく。
                // 保護ビュー・スマホ・プレビューなど、計算し直さずに開いても値が出るようにするため
                evaluateFormulas(workbook);
                // Excel で開いたときにも必ず計算し直させる（ここで計算できなかった式のため）
                workbook.setForceFormulaRecalculation(true);
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("作業報告書の生成に失敗しました。", e);
        }
    }

    @Override
    public byte[] generateYear(int year, String employeeName, List<WorkType> workTypes, List<MonthData> months) {
        return generateYear(year, employeeName, null, workTypes, months);
    }

    @Override
    public byte[] generateYear(int year, String employeeName, String departmentName, List<WorkType> workTypes,
                               List<MonthData> months) {
        return generateYear(year, employeeName, departmentName, workTypes, months, null);
    }

    @Override
    public byte[] generateYear(int year, String employeeName, String departmentName, List<WorkType> workTypes,
                               List<MonthData> months, YearMonth focusMonth) {
        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new IllegalStateException("作業報告書テンプレートが見つかりません: " + TEMPLATE_PATH);
            }
            try (XSSFWorkbook workbook = new XSSFWorkbook(templateStream)) {
                // すべての月のシート（前年12月・1月〜12月）に年と氏名を入れる。データのない月も年がずれないように
                for (int m = 0; m <= 12; m++) {
                    YearMonth ym = m == 0 ? YearMonth.of(year - 1, 12) : YearMonth.of(year, m);
                    Sheet sheet = sheetOf(workbook, ym, year);
                    setNumericValue(sheet, "A1", ym.getYear());
                    setStringValue(sheet, "AP2", employeeName);
                    setDepartment(sheet, departmentName);
                }
                for (MonthData month : months) {
                    YearMonth ym = month.monthlyData().getYearMonth();
                    writeMonth(sheetOf(workbook, ym, year), month.monthlyData(), month.weeklyWorkReports(), workTypes);
                }
                if (focusMonth != null) {
                    focus(workbook, focusMonth, year, LocalDate.now());
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // 様式の数式（総勤務・残業・深夜など）の計算結果を、出力のときに入れておく。
                // 保護ビュー・スマホ・プレビューなど、計算し直さずに開いても値が出るようにするため
                evaluateFormulas(workbook);
                // Excel で開いたときにも必ず計算し直させる（ここで計算できなかった式のため）
                workbook.setForceFormulaRecalculation(true);
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("作業報告書の生成に失敗しました。", e);
        }
    }

    /**
     * 開いたときに、その月のシートの「その週」を表示する。
     *   その週：今日がその月なら今日を含む週（木曜〜水曜。週次作業報告の週と同じ）、そうでなければ第1週
     *   画面の一番上はその週の最初の日の行（週次作業報告の欄が上にあればそこから）。選ぶセルはその日の「出勤」
     * ほかのシート（tool など）の選択は外す。
     */
    static void focus(XSSFWorkbook workbook, YearMonth yearMonth, int year, LocalDate today) {
        Sheet sheet = sheetOf(workbook, yearMonth, year);
        if (sheet == null) {
            return;
        }
        LocalDate first = yearMonth.atDay(1);
        LocalDate target = YearMonth.from(today).equals(yearMonth) ? today : first;
        List<LocalDate[]> weeks = WeeklyWorkReport.resolveWeekRanges(yearMonth);
        int week = 0;
        for (int i = 0; i < weeks.size(); i++) {
            if (!target.isBefore(weeks.get(i)[0]) && !target.isAfter(weeks.get(i)[1])) {
                week = i;
                break;
            }
        }
        LocalDate weekStart = weeks.get(week)[0].isBefore(first) ? first : weeks.get(week)[0];
        int startRow = FIRST_DAY_ROW + weekStart.getDayOfMonth() - 1;           // 0始まり
        int blockRow = WEEK_BLOCK_FIRST_ROWS[Math.min(week, WEEK_BLOCK_FIRST_ROWS.length - 1)] - 1;
        int topRow = Math.min(startRow, blockRow);
        int selectRow = FIRST_DAY_ROW + target.getDayOfMonth() - 1;

        int index = workbook.getSheetIndex(sheet);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            workbook.getSheetAt(i).setSelected(i == index);
        }
        workbook.setActiveSheet(index);
        sheet.setActiveCell(new org.apache.poi.ss.util.CellAddress(selectRow, 4)); // E列（出勤）
        // 枠の固定がない様式なので、表示の左上のセルを直接決める（showInPane は枠の固定があるときだけ効く）
        ((org.apache.poi.xssf.usermodel.XSSFSheet) sheet).getCTWorksheet().getSheetViews().getSheetViewArray(0)
                .setTopLeftCell(new org.apache.poi.ss.util.CellReference(topRow, 0).formatAsString(false));
    }

    /**
     * すべての数式を計算して、計算結果をファイルに入れる。
     * 1つの式が計算できなくても（POI が対応していない関数など）ほかの式は計算する。計算できなかった式は前の値のまま
     * （Excel で開けば計算し直される）。
     * @return 計算できなかった式の数
     */
    static int evaluateFormulas(XSSFWorkbook workbook) {
        org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        int failed = 0;
        RuntimeException firstError = null;
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.FORMULA) {
                        continue;
                    }
                    try {
                        evaluator.evaluateFormulaCell(cell);
                    } catch (RuntimeException e) {
                        failed++;
                        if (firstError == null) firstError = e;
                    }
                }
            }
        }
        if (failed > 0) {
            java.util.logging.Logger.getLogger(WorkReportXlsmExportAdapter.class.getName()).log(java.util.logging.Level.WARNING,
                    "作業報告書の式のうち " + failed + " 個を計算できませんでした（Excel で開いたときに計算します）", firstError);
        }
        return failed;
    }

    /** 月のシート。当年の月は「○月」、前年の12月は「前年12月」 */
    private static Sheet sheetOf(XSSFWorkbook workbook, YearMonth yearMonth, int year) {
        String sheetName = yearMonth.getYear() == year - 1 && yearMonth.getMonthValue() == 12
                ? "前年12月" : yearMonth.getMonthValue() + "月";
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalStateException("テンプレートにシートが見つかりません: " + sheetName);
        }
        return sheet;
    }

    /** 1か月分の勤怠（各日の行）と週次作業報告を、その月のシートに書く */
    private void writeMonth(Sheet sheet, MonthlyAttendanceDto monthlyData, List<WeeklyWorkReport> weeklyWorkReports,
                            List<WorkType> workTypes) {
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
        writeWeeklyWorkReports(sheet, monthlyData.getYearMonth(), weeklyWorkReports);
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
     * AR/AS/AT列の該当ブロックへ書き込む。リーダー所見（AU列）は、取り込んだ所見があればそのまま書き戻す。
     */
    private void writeWeeklyWorkReports(Sheet sheet, YearMonth yearMonth, List<WeeklyWorkReport> weeklyWorkReports) {
        if (weeklyWorkReports == null) {
            return;
        }
        List<LocalDate[]> ranges = WeeklyWorkReport.resolveWeekRanges(yearMonth);
        for (int i = 0; i < weeklyWorkReports.size(); i++) {
            WeeklyWorkReport report = weeklyWorkReports.get(i);
            // 週の終わりの日で、その月の何週目の欄かを決める（空の週がとばされた並びでも、ずれないように）
            int block = i;
            for (int w = 0; w < ranges.size(); w++) {
                if (ranges.get(w)[1].equals(report.getWeekEndDate())) {
                    block = w;
                    break;
                }
            }
            if (block >= WEEK_BLOCK_FIRST_ROWS.length) {
                continue;
            }
            int baseRow = WEEK_BLOCK_FIRST_ROWS[block]; // Excel行番号（1始まり）

            if (hasText(report.getClientName())) {
                setStringValue(sheet, "AR" + baseRow, fit(report.getClientName(), LINE_WIDTH));
            }
            if (hasText(report.getProjectName())) {
                setStringValue(sheet, "AR" + (baseRow + 1), fit("・" + report.getProjectName(), LINE_WIDTH));
            }
            writeLines(sheet, "AR", baseRow + 2, MAX_LINES_PROCESS_NAME, report.getProcessName(), "　");

            if (hasText(report.getProgressStatus())) {
                setStringValue(sheet, "AS" + baseRow, fit("作業進捗：" + report.getProgressStatus(), LINE_WIDTH));
            }
            writeLines(sheet, "AS", baseRow + 1, 6, report.getProgressDetail(), "");
            writeProblemCountermeasure(sheet, baseRow, report.getProblemCountermeasure());

            writeLines(sheet, "AT", baseRow, 10, report.getPersonalNote(), "");
            // リーダー所見は取り込んだ文をそのまま戻す（行を送らない。取り込みのときに行がそのまま入っているため）
            writeLines(sheet, "AU", baseRow, 10, report.getLeaderComment(), "", Integer.MAX_VALUE);
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
        if (lines.size() == 1 && width(PROBLEM_LABEL + lines.get(0).trim()) <= LINE_WIDTH) {
            setStringValue(sheet, "AS" + (baseRow + 7), PROBLEM_LABEL + lines.get(0).trim());
        } else {
            writeLines(sheet, "AS", baseRow + 8, 2, String.join("\n", lines), "");
        }
    }

    /**
     * 改行区切りのテキストを、指定列の連続する行に1行ずつ書き込む（最大 maxLines 行）。
     * 各行の先頭に prefix（様式の字下げ）を付ける。ただし空白で始まる行には付けない（字下げが二重にならないように）。
     */
    private void writeLines(Sheet sheet, String column, int startRow, int maxLines, String text, String prefix) {
        writeLines(sheet, column, startRow, maxLines, text, prefix, LINE_WIDTH);
    }

    private void writeLines(Sheet sheet, String column, int startRow, int maxLines, String text, String prefix, int width) {
        if (!hasText(text)) {
            return;
        }
        List<String> lines = layoutLines(text, prefix, width, maxLines);
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                setStringValue(sheet, column + (startRow + i), lines.get(i));
            }
        }
    }

    /**
     * 書く行を決める。セルの幅（width）に収まる行はそのまま、長い行は文字を切らずに次の行へ送る。
     * maxLines 行を超えるときだけ、最後の行の末尾を「…」にする。
     */
    static List<String> layoutLines(String text, String prefix, int width, int maxLines) {
        List<String> out = new ArrayList<>();
        String[] source = text.split("\\r?\\n", -1);
        int end = source.length;
        while (end > 0 && source[end - 1].isBlank()) end--; // 末尾の空行は書かない
        boolean overflow = false;
        for (int i = 0; i < end && !overflow; i++) {
            String line = source[i];
            String head = line.isBlank() || line.isEmpty() || Character.isWhitespace(line.charAt(0)) || line.charAt(0) == '　'
                    ? "" : prefix;
            List<String> pieces = wrap(head + line, width);
            for (String piece : pieces) {
                if (out.size() == maxLines) {
                    overflow = true;
                    break;
                }
                out.add(piece);
            }
        }
        if (overflow && !out.isEmpty()) {
            int last = out.size() - 1;
            out.set(last, fit(out.get(last) + "…", width));
        }
        return out;
    }

    /** 1行を幅 width ごとに分ける（空の行は空の1行） */
    static List<String> wrap(String line, int width) {
        List<String> pieces = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int w = 0;
        for (int i = 0; i < line.length(); ) {
            int cp = line.codePointAt(i);
            int cw = charWidth(cp);
            if (w + cw > width && current.length() > 0) {
                pieces.add(current.toString());
                current.setLength(0);
                w = 0;
            }
            current.appendCodePoint(cp);
            w += cw;
            i += Character.charCount(cp);
        }
        pieces.add(current.toString());
        return pieces;
    }

    /** 幅 width に収まらなければ、末尾を「…」にして収める */
    static String fit(String value, int width) {
        if (value == null || width(value) <= width) {
            return value;
        }
        String s = value;
        while (!s.isEmpty() && width(s) + charWidth('…') > width) {
            s = s.substring(0, s.length() - Character.charCount(s.codePointBefore(s.length())));
        }
        return s + "…";
    }

    /** 表示の幅（半角1・全角2） */
    static int width(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            w += charWidth(cp);
            i += Character.charCount(cp);
        }
        return w;
    }

    /** 半角（ASCII・ラテン文字・半角カナ）は1、それ以外（漢字・かな・全角記号など）は2 */
    static int charWidth(int cp) {
        if (cp < 0x0370 || (cp >= 0xFF61 && cp <= 0xFF9F)) return 1;
        return 2;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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

    /** 「所属：」欄（D2）。null・空なら様式のまま */
    private void setDepartment(Sheet sheet, String departmentName) {
        if (departmentName != null && !departmentName.isBlank()) {
            setStringValue(sheet, "D2", departmentName);
        }
    }
}
