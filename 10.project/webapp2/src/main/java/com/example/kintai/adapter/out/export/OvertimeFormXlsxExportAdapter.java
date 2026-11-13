package com.example.kintai.adapter.out.export;

import com.example.kintai.domain.port.out.OvertimeFormExportPort;
import com.example.kintai.domain.service.OvertimeFormCalculator.ApprovalMark;
import com.example.kintai.domain.service.OvertimeFormCalculator.TimeCells;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * 個人別時間外勤務申請兼実績報告書（様式 9版）のテンプレートに値を書き込む。
 * 時間の計算・累計・摘要別の集計はテンプレートの式のまま（開いたときに Excel が計算する）。
 */
public class OvertimeFormXlsxExportAdapter implements OvertimeFormExportPort {

    static final String TEMPLATE = "/templates/overtime_request_template.xlsx";
    static final int FIRST_ROW = 10;
    private static final String SKIPPED = "－";
    private static final String[] PLANNED_APPROVAL_COLUMNS = {"P", "Q", "R"};
    private static final String[] ACTUAL_APPROVAL_COLUMNS = {"AD", "AE", "AF"};

    @Override
    public byte[] write(OvertimeForm form) {
        try (InputStream in = OvertimeFormXlsxExportAdapter.class.getResourceAsStream(TEMPLATE)) {
            if (in == null) {
                throw new IllegalStateException("時間外申請書のテンプレートがありません: " + TEMPLATE);
            }
            try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                Sheet sh = wb.getSheetAt(0);
                setDate(sh, "O1", form.month().atDay(1));
                setText(sh, "C2", form.departmentName());
                setText(sh, "C3", form.staffName());
                setText(sh, "I3", form.sectionChiefName());
                setText(sh, "I4", form.departmentHeadName());
                List<FormRow> rows = form.rows();
                for (int i = 0; i < rows.size() && i < MAX_ROWS; i++) {
                    writeRow(sh, FIRST_ROW + i, rows.get(i));
                }
                wb.setForceFormulaRecalculation(true);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                wb.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new IllegalStateException("時間外申請書を作れませんでした: " + e.getMessage(), e);
        }
    }

    private static void writeRow(Sheet sh, int r, FormRow row) {
        setDate(sh, "A" + r, row.date());
        setText(sh, "C" + r, row.projectName());
        writeTimes(sh, r, row.planned(), "D", "E", "F", "G", "I", "J");
        setText(sh, "O" + r, row.category());
        writeApprovals(sh, r, row.approvals(), PLANNED_APPROVAL_COLUMNS);
        writeTimes(sh, r, row.actual(), "S", "T", "U", "V", "X", "Y");
        if (row.actualApprovals() != null) {
            // 実績報告の承認：実績の確認日（勤怠の入力日）か、申請し直して承認された日
            writeApprovals(sh, r, row.actualApprovals(), ACTUAL_APPROVAL_COLUMNS);
        } else if (!row.actual().isEmpty()) {
            writeApprovals(sh, r, row.approvals(), ACTUAL_APPROVAL_COLUMNS);
        }
        setText(sh, "AG" + r, row.remarks());
    }

    private static void writeTimes(Sheet sh, int r, TimeCells t, String startCol, String endCol,
                                   String signCol, String adjustCol, String nightSignCol, String nightAdjustCol) {
        if (t.span() != null) {
            setTime(sh, startCol + r, t.span().start());
            setTime(sh, endCol + r, t.span().end());
        }
        writeAdjust(sh, r, t.adjustMinutes(), signCol, adjustCol);
        writeAdjust(sh, r, t.nightAdjustMinutes(), nightSignCol, nightAdjustCol);
    }

    private static void writeAdjust(Sheet sh, int r, int minutes, String signCol, String adjustCol) {
        if (minutes != 0) {
            setText(sh, signCol + r, minutes > 0 ? "+" : "-");
            setTime(sh, adjustCol + r, Math.abs(minutes));
        }
    }

    private static void writeApprovals(Sheet sh, int r, List<ApprovalMark> marks, String[] columns) {
        for (int i = 0; i < columns.length && i < marks.size(); i++) {
            ApprovalMark m = marks.get(i);
            if (m.skipped()) {
                setText(sh, columns[i] + r, SKIPPED);
            } else if (m.date() != null) {
                setDate(sh, columns[i] + r, m.date());
            }
        }
    }

    private static Cell cell(Sheet sh, String ref) {
        CellReference cr = new CellReference(ref);
        Row row = sh.getRow(cr.getRow());
        if (row == null) {
            row = sh.createRow(cr.getRow());
        }
        Cell c = row.getCell(cr.getCol());
        return c != null ? c : row.createCell(cr.getCol());
    }

    private static void setText(Sheet sh, String ref, String value) {
        if (value != null && !value.isBlank()) {
            cell(sh, ref).setCellValue(value);
        }
    }

    private static void setDate(Sheet sh, String ref, LocalDate date) {
        if (date != null) {
            cell(sh, ref).setCellValue(date);
        }
    }

    /** 分を Excel の時刻（1日=1）で入れる。24時以降は1を超える */
    private static void setTime(Sheet sh, String ref, int minutes) {
        cell(sh, ref).setCellValue(minutes / 1440.0);
    }
}
