package com.example.kintai.adapter.out.export;

import com.example.kintai.domain.model.attendance.OvertimeFormApprovals;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals.Mark;
import com.example.kintai.domain.model.attendance.OvertimeFormApprovals.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 時間外申請書（個人別時間外勤務申請書兼実績報告書・.xlsx）の承認欄を読む。
 *   氏名 … C3
 *   明細 … 10行目から。A列が対象日、P〜R列が申請の承認欄、AD〜AF列が実績報告の承認欄（課長／所属長／社長）
 * 承認欄は、日付なら承認日、「－」などの横線ならルートにない段、それ以外は空欄として読む。
 */
public class OvertimeFormXlsxImportAdapter {

    private static final int FIRST_ROW = 9;   // 0始まり（Excel の10行目）
    private static final int LAST_ROW = 45;   // これより下は注意書き
    private static final String[] PLANNED = {"P", "Q", "R"};
    private static final String[] ACTUAL = {"AD", "AE", "AF"};
    private static final Pattern DASH = Pattern.compile("^[-－ー―‐−─]+$");
    private static final Pattern YMD = Pattern.compile("^(\\d{4})[/.-](\\d{1,2})[/.-](\\d{1,2})$");
    private static final Pattern MD = Pattern.compile("^(\\d{1,2})/(\\d{1,2})$");

    public OvertimeFormApprovals parse(InputStream in) {
        try (Workbook book = WorkbookFactory.create(in)) {
            Sheet sheet = book.getSheetAt(0);
            String name = text(cell(sheet, 2, 2)); // C3
            List<Row> rows = new ArrayList<>();
            for (int r = FIRST_ROW; r <= LAST_ROW; r++) {
                LocalDate date = date(cell(sheet, r, 0), null);
                if (date == null) {
                    continue;
                }
                rows.add(new Row(date, marks(sheet, r, PLANNED, date), marks(sheet, r, ACTUAL, date)));
            }
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("明細（10行目から）に日付がありません。時間外申請書（個人別時間外勤務申請書）か確かめてください。");
            }
            return new OvertimeFormApprovals(name == null ? "" : name.trim(), rows);
        } catch (IOException e) {
            throw new IllegalArgumentException("Excel ファイルとして読めませんでした。", e);
        }
    }

    private static List<Mark> marks(Sheet sheet, int r, String[] columns, LocalDate rowDate) {
        List<Mark> marks = new ArrayList<>();
        for (String col : columns) {
            Cell c = cell(sheet, r, CellReference.convertColStringToIndex(col));
            LocalDate d = date(c, rowDate);
            if (d != null) {
                marks.add(new Mark(d, false));
            } else {
                String t = text(c);
                marks.add(t != null && DASH.matcher(t.trim()).matches() ? Mark.SKIPPED : Mark.BLANK);
            }
        }
        return marks;
    }

    private static Cell cell(Sheet sheet, int r, int c) {
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
        return row == null ? null : row.getCell(c);
    }

    /** 日付のセル（日付の数値・「2026/11/6」・「11/6」）。年がなければ対象日の年を使う */
    static LocalDate date(Cell c, LocalDate base) {
        if (c == null) return null;
        CellType type = c.getCellType() == CellType.FORMULA ? c.getCachedFormulaResultType() : c.getCellType();
        if (type == CellType.NUMERIC) {
            double v = c.getNumericCellValue();
            if (v > 30000 && v < 80000) { // 1982年〜2119年の日付の数値
                return DateUtil.getLocalDateTime(v).toLocalDate();
            }
            return null;
        }
        if (type == CellType.STRING) {
            String t = c.getStringCellValue().trim();
            Matcher m = YMD.matcher(t);
            if (m.matches()) {
                return LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
            }
            m = MD.matcher(t);
            if (m.matches() && base != null) {
                return LocalDate.of(base.getYear(), Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            }
        }
        return null;
    }

    private static String text(Cell c) {
        if (c == null) return null;
        CellType type = c.getCellType() == CellType.FORMULA ? c.getCachedFormulaResultType() : c.getCellType();
        return type == CellType.STRING ? c.getStringCellValue() : null;
    }
}
